package com.vladimirpandurov.spring_security_invoice_manager.resource;

import com.vladimirpandurov.spring_security_invoice_manager.domain.HttpResponse;
import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import com.vladimirpandurov.spring_security_invoice_manager.form.LoginForm;
import com.vladimirpandurov.spring_security_invoice_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(path="/user")
@RequiredArgsConstructor
@Slf4j
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
        UserDTO userDTO = userService.getUserByEmail(loginForm.getEmail());
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }


    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO){
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDTO))
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


    private URI getUri(Long userId){
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/" + userId).toUriString());
    }

}
