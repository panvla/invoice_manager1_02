package com.vladimirpandurov.spring_security_invoice_manager.resource;

import com.vladimirpandurov.spring_security_invoice_manager.domain.HttpResponse;
import com.vladimirpandurov.spring_security_invoice_manager.domain.User;
import com.vladimirpandurov.spring_security_invoice_manager.domain.UserPrincipal;
import com.vladimirpandurov.spring_security_invoice_manager.dto.UserDTO;
import com.vladimirpandurov.spring_security_invoice_manager.dtomapper.UserDTOMapper;
import com.vladimirpandurov.spring_security_invoice_manager.exception.ApiException;
import com.vladimirpandurov.spring_security_invoice_manager.form.LoginForm;
import com.vladimirpandurov.spring_security_invoice_manager.provider.TokenProvider;
import com.vladimirpandurov.spring_security_invoice_manager.service.RoleService;
import com.vladimirpandurov.spring_security_invoice_manager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import static com.vladimirpandurov.spring_security_invoice_manager.utils.ExceptionUtils.processError;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private static final String TOKEN_PREFIX = "Bearer ";

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
    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) {
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .message("Email sent. Please check your email to reset your password.")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }
    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key){
        UserDTO user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", user))
                .message("Please enter a new password")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }
    @PostMapping("/resetpassword/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> resetPasswordWithUrl(@PathVariable("key") String key, @PathVariable("password") String password, @PathVariable("confirmPassword") String confirmPassword){
        userService.renewPassword(key, password, confirmPassword);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .message("Password reset successfully")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }
    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key){
        UserDTO userDTO = userService.verifyAccount(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .data(Map.of("user", userDTO))
                .message("Profile Retrieved")
                .message(userDTO.isEnabled() ? "Account already verified " : "Account verified")
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .build()
        );
    }
    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request){
        if(isHeaderAndTokenValid(request)){
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO user = userService.getUserByEmail(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                    .timeStamp(LocalDateTime.now().toString())
                    .data(Map.of("user", user,
                            "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                            "refresh_token", token))
                    .message("Token refreshed")
                    .status(HttpStatus.OK)
                    .statusCode(HttpStatus.OK.value())
                    .build()
            );
        }else {
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                    .timeStamp(LocalDateTime.now().toString())
                    .reason("Refresh Token missing or invalid")
                    .status(HttpStatus.BAD_REQUEST)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .build()
            );
        }
    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        return (request.getHeader(AUTHORIZATION) != null) &&
                (request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)) &&
                (tokenProvider.isTokenValid(tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()),request),
                        request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length())));
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request){
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                .timeStamp(LocalDateTime.now().toString())
                .reason("An error occurred " + request.getMethod())
                .status(HttpStatus.NOT_FOUND)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build()
        );
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

    private UserDTO getAuthenticatedUser(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    private Authentication authenticate(String email, String password){
        try{
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
            return authentication;
        }catch (Exception exception){
            processError(request, response, exception);
            throw new ApiException(exception.getMessage());
        }
    }



}
