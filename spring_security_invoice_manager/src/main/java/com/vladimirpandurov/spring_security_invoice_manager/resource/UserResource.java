package com.vladimirpandurov.spring_security_invoice_manager.resource;

import com.vladimirpandurov.spring_security_invoice_manager.domain.HttpResponse;
import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.domain.UserPrincipal;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import com.vladimirpandurov.spring_security_invoice_manager.dtomapper.UserDTOMapper;
import com.vladimirpandurov.spring_security_invoice_manager.form.LoginForm;
import com.vladimirpandurov.spring_security_invoice_manager.provider.TokenProvider;
import com.vladimirpandurov.spring_security_invoice_manager.service.RoleService;
import com.vladimirpandurov.spring_security_invoice_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path="/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){
        UserDTO userDTO = this.userService.createUser(user);
        return ResponseEntity.created(getUri(userDTO.getId())).body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", userDTO))
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .message("User created")
                .build()
        );
    }
    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        authenticationManager.authenticate(unauthenticated(loginForm.getEmail(), loginForm.getPassword()));
        UserDTO userDTO = userService.getUserByEmail(loginForm.getEmail());
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }
    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code){
        UserDTO user = userService.verifyCode(email, code);
        return sendResponse(user);
    }

    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDTO userDTO = userService.getUserByEmail(authentication.getName());
        log.info(authentication.getName());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", userDTO))
                        .message("Profile Retrieved")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }


    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDTO, "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login Success")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO){
        this.userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDTO))
                        .message("Verification code sent")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private UserPrincipal getUserPrincipal(UserDTO userDTO){
        return new UserPrincipal(UserDTOMapper.toUser(userService.getUserByEmail(userDTO.getEmail())), this.roleService.getRoleByUserId(userDTO.getId()));
    }

    private URI getUri(Long userId){
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/" + userId).toUriString());
    }

}
