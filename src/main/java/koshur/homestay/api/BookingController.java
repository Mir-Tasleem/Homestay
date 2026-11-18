package koshur.homestay.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import koshur.homestay.api.dto.BookingRequest;
import koshur.homestay.api.dto.BookingResponse;
import koshur.homestay.service.PersistentBookingService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
public class BookingController {
    private final PersistentBookingService bookings;
    public BookingController(PersistentBookingService bookings) { this.bookings = bookings; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody BookingRequest request) {
        return bookings.create(userId, request);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return bookings.get(userId, id);
    }

    @GetMapping("/my-bookings")
    public Page<BookingResponse> mine(@AuthenticationPrincipal UUID userId,
                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return bookings.mine(userId, page, size);
    }

    @PutMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return bookings.cancel(userId, id);
    }
}
