package com.chat_room_app.auth.v2;

import com.chat_room_app.auth.dtos.RegisterUserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auths")
public class AuthV2Controller {

    private final AuthV2Service authV2Service;

    public AuthV2Controller(AuthV2Service authV2Service) {
        this.authV2Service = authV2Service;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterUserDto registerUserDto) {
        authV2Service.signUp(registerUserDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/resend-verification/{username}")
    public ResponseEntity<String> resendVerification(@PathVariable String username) {
        authV2Service.resendVerificationEmail(username);
        return ResponseEntity.ok("Verification email sent");
    }

    @PostMapping("/forgot/{username}")
    public ResponseEntity<String> forgotPassword(@PathVariable String username) {
        authV2Service.sendForgotPasswordVerificationCode(username);
        return ResponseEntity.ok("Forgot password verification code sent");
    }

}
