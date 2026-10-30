package com.auth.app.dto;

public record LoginRequest(
        String email,
        String password
) {
}
