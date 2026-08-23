package br.edu.cpa.cpainteligente.dto.response;

public record AuthResponse(
        String token,
        String userId,
        String name,
        String email,
        String institutionId
) {}
