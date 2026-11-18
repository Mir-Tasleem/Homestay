package koshur.homestay.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = {@Index(name = "idx_bookings_property_dates", columnList = "property_id,check_in,check_out,status"),
        @Index(name = "idx_bookings_user", columnList = "user_id")})
public class BookingEntity {
    public enum Status { CONFIRMED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity property;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private AppUser guest;
    @Column(name = "check_in", nullable = false) private LocalDate checkIn;
    @Column(name = "check_out", nullable = false) private LocalDate checkOut;
    @Column(name = "guest_count", nullable = false) private int guestCount;
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2) private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.CONFIRMED;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected BookingEntity() {}
    public BookingEntity(PropertyEntity property, AppUser guest, LocalDate checkIn, LocalDate checkOut,
                         int guestCount, BigDecimal totalAmount) {
        this.property = property; this.guest = guest; this.checkIn = checkIn;
        this.checkOut = checkOut; this.guestCount = guestCount; this.totalAmount = totalAmount;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public void cancel() { status = Status.CANCELLED; }
    public UUID getId() { return id; }
    public PropertyEntity getProperty() { return property; }
    public AppUser getGuest() { return guest; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
