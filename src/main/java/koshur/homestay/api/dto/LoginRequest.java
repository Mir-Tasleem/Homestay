package koshur.homestay.api.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank @Email @Size(max = 320) String email,
                           @NotBlank String password) {}
