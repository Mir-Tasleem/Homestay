package koshur.homestay.api.dto;

import java.util.UUID;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds,
                            UUID userId, String email, String displayName) {}
