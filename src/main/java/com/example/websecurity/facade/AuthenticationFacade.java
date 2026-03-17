package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.security.JwtService;
import com.example.websecurity.service.LoginAttemptService;
import com.example.websecurity.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthenticationResponse authenticate(@NotNull AuthenticationRequest request) {
        log.info("Authentication Facade: Authenticating user with request: {}", request);

        if (loginAttemptService.isBlocked(request.getEmail())) {
            LocalDateTime until = loginAttemptService.getLockoutUntil(request.getEmail());
            long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), until);
            throw new LockedException("Previše neuspelih pokušaja. Pokušajte ponovo za " + secondsLeft + " sekundi.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            loginAttemptService.loginSucceeded(request.getEmail());
        } catch (AuthenticationException  e) {
            loginAttemptService.loginFailed(request.getEmail());
            throw e;
        }

        var user = userService.getUserByEmail(request.getEmail());
        var accessToken = jwtService.generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}