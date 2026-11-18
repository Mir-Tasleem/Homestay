package koshur.homestay.api.dto;

import koshur.homestay.persistence.BookingEntity;
import koshur.homestay.persistence.BookingEntity.Status;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(UUID id, UUID propertyId, UUID guestId, LocalDate checkIn,
                              LocalDate checkOut, int guestCount, Status status, Instant createdAt) {
    public static BookingResponse from(BookingEntity booking) {
        return new BookingResponse(booking.getId(), booking.getProperty().getId(), booking.getGuest().getId(),
                booking.getCheckIn(), booking.getCheckOut(), booking.getGuestCount(), booking.getStatus(), booking.getCreatedAt());
    }
}
