package com.dochiri.userservice.presentation;

import com.dochiri.userservice.application.RegisterUserService;
import com.dochiri.userservice.presentation.request.RegisterUserRequest;
import com.dochiri.userservice.presentation.response.RegisterUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserService registerUserService;

    @PostMapping
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.ok().body(
                RegisterUserResponse.from(
                        registerUserService.register(
                                request.toCommand()
                        )
                )
        );
    }

}