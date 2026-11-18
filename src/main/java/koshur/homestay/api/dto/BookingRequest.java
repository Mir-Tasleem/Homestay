package koshur.homestay.api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record BookingRequest(@NotNull UUID propertyId,
                             @NotNull @FutureOrPresent LocalDate checkIn,
                             @NotNull @Future LocalDate checkOut,
                             @Min(1) @Max(100) int guestCount) {}
