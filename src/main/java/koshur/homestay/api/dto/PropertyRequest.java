package koshur.homestay.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PropertyRequest(@NotBlank @Size(max = 160) String name,
                              @NotBlank @Size(max = 2000) String description,
                              @NotBlank @Size(max = 120) String city,
                              @Min(1) @Max(100) int maxGuests,
                              @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal nightlyRate) {}
