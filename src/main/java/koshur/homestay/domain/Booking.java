package koshur.homestay.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** A booking uses a half-open date range: check-in inclusive, check-out exclusive. */
public record Booking(UUID id, UUID propertyId, UUID guestId, LocalDate checkIn, LocalDate checkOut,
                      int guestCount) {
    public Booking {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(propertyId, "propertyId");
        Objects.requireNonNull(guestId, "guestId");
        Objects.requireNonNull(checkIn, "checkIn");
        Objects.requireNonNull(checkOut, "checkOut");
        if (!checkOut.isAfter(checkIn)) throw new IllegalArgumentException("checkOut must be after checkIn");
        if (guestCount < 1) throw new IllegalArgumentException("guestCount must be positive");
    }

    public static Booking create(UUID propertyId, UUID guestId, LocalDate checkIn, LocalDate checkOut,
                                 int guestCount) {
        return new Booking(UUID.randomUUID(), propertyId, guestId, checkIn, checkOut, guestCount);
    }

    public boolean overlaps(LocalDate start, LocalDate end) {
        return checkIn.isBefore(end) && start.isBefore(checkOut);
    }
}
