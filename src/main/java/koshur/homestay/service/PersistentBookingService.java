package koshur.homestay.service;

import koshur.homestay.api.ApiException;
import koshur.homestay.api.dto.BookingRequest;
import koshur.homestay.api.dto.BookingResponse;
import koshur.homestay.persistence.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class PersistentBookingService {
    private final BookingRepository bookings;
    private final PropertyRepository properties;
    private final UserRepository users;

    public PersistentBookingService(BookingRepository bookings, PropertyRepository properties, UserRepository users) {
        this.bookings = bookings; this.properties = properties; this.users = users;
    }

    @Transactional
    public BookingResponse create(UUID guestId, BookingRequest request) {
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "checkOut must be after checkIn");
        }
        PropertyEntity property = properties.findByIdForUpdate(request.propertyId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
        if (request.guestCount() > property.getMaxGuests()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "guestCount exceeds property capacity");
        }
        if (bookings.existsOverlapping(property.getId(), request.checkIn(), request.checkOut(), BookingEntity.Status.CONFIRMED)) {
            throw new ApiException(HttpStatus.CONFLICT, "Property is already booked for overlapping dates");
        }
        AppUser guest = users.findById(guestId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User account no longer exists"));
        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal total = property.getNightlyRate().multiply(BigDecimal.valueOf(nights));
        return BookingResponse.from(bookings.save(new BookingEntity(property, guest,
                request.checkIn(), request.checkOut(), request.guestCount(), total)));
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID guestId, UUID bookingId) {
        return BookingResponse.from(ownedBooking(guestId, bookingId));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> mine(UUID guestId, int page, int size) {
        return bookings.findByGuest_Id(guestId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(BookingResponse::from);
    }

    @Transactional
    public BookingResponse cancel(UUID guestId, UUID bookingId) {
        BookingEntity booking = ownedBooking(guestId, bookingId);
        booking.cancel();
        return BookingResponse.from(booking);
    }

    private BookingEntity ownedBooking(UUID guestId, UUID bookingId) {
        return bookings.findByIdAndGuest_Id(bookingId, guestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}
