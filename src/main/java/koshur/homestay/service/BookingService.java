package koshur.homestay.service;

import koshur.homestay.domain.Booking;
import koshur.homestay.domain.Property;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingService {
    Booking book(Property property, UUID guestId, LocalDate checkIn, LocalDate checkOut, int guestCount);
    boolean isAvailable(UUID propertyId, LocalDate checkIn, LocalDate checkOut);
    Optional<Booking> findBooking(UUID bookingId);
    List<Booking> bookingsFor(UUID propertyId);
}
