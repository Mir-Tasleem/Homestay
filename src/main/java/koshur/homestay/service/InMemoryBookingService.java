package koshur.homestay.service;

import koshur.homestay.domain.Booking;
import koshur.homestay.domain.Property;
import koshur.homestay.domain.PropertyUnavailableException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory example. ConcurrentHashMap.compute makes overlap-check plus insert atomic per property. */
public class InMemoryBookingService implements BookingService {
    private final ConcurrentMap<UUID, List<Booking>> bookingsByProperty = new ConcurrentHashMap<>();

    @Override
    public Booking book(Property property, UUID guestId, LocalDate checkIn, LocalDate checkOut, int guestCount) {
        Booking candidate = Booking.create(property.getId(), guestId, checkIn, checkOut, guestCount);
        if (guestCount > property.getMaxGuests()) {
            throw new IllegalArgumentException("guestCount exceeds property capacity");
        }
        bookingsByProperty.compute(property.getId(), (propertyId, existing) -> {
            List<Booking> current = existing == null ? List.of() : existing;
            if (current.stream().anyMatch(booking -> booking.overlaps(checkIn, checkOut))) {
                throw new PropertyUnavailableException(propertyId);
            }
            return java.util.stream.Stream.concat(current.stream(), java.util.stream.Stream.of(candidate)).toList();
        });
        return candidate;
    }

    @Override
    public boolean isAvailable(UUID propertyId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return bookingsByProperty.getOrDefault(propertyId, List.of()).stream()
                .noneMatch(booking -> booking.overlaps(checkIn, checkOut));
    }

    @Override
    public Optional<Booking> findBooking(UUID bookingId) {
        return bookingsByProperty.values().stream().flatMap(List::stream)
                .filter(booking -> booking.id().equals(bookingId)).findFirst();
    }

    @Override
    public List<Booking> bookingsFor(UUID propertyId) {
        return List.copyOf(bookingsByProperty.getOrDefault(propertyId, List.of()));
    }

    private static void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }
}
