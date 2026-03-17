package com.example.websecurity.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.Data;

@Service
public class LoginAttemptService {

    @Data
    public class LoginAttemptData {
        private int attempts = 0;
        private LocalDateTime lockoutUntil;

        public void incrementAttempts() { this.attempts++; }
    }

    private final int MAX_ATTEMPTS = 5;
    // čuva email -> broj pokušaja i vreme lockout-a
    private final Map<String, LoginAttemptData> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        LoginAttemptData data = attempts.getOrDefault(email, new LoginAttemptData());
        data.incrementAttempts();
        data.setLockoutUntil(calculateLockout(data.getAttempts()));
        attempts.put(email, data);
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public boolean isBlocked(String email) {
        LoginAttemptData data = attempts.get(email);
        if (data == null) return false;
        return data.getLockoutUntil() != null 
            && data.getLockoutUntil().isAfter(LocalDateTime.now());
    }

    public LocalDateTime getLockoutUntil(String email) {
        return attempts.get(email).getLockoutUntil();
    }

    private LocalDateTime calculateLockout(int attempts) {
        // 1->3s, 2->10s, 3->30s, 4->60s, 5+->300s
        long seconds = switch (attempts) {
            case 1 -> 3;
            case 2 -> 10;
            case 3 -> 30;
            case 4 -> 60;
            default -> 300;
        };
        return LocalDateTime.now().plusSeconds(seconds);
    }
}

