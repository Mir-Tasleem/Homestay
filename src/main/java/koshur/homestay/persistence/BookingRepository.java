package koshur.homestay.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    boolean existsByProperty_Id(UUID propertyId);
    @Query("select count(b) > 0 from BookingEntity b where b.property.id = :propertyId " +
            "and b.status = :status " +
            "and b.checkIn < :checkOut and :checkIn < b.checkOut")
    boolean existsOverlapping(@Param("propertyId") UUID propertyId,
                              @Param("checkIn") LocalDate checkIn,
                              @Param("checkOut") LocalDate checkOut,
                              @Param("status") BookingEntity.Status status);
    Optional<BookingEntity> findByIdAndGuest_Id(UUID id, UUID guestId);
    Page<BookingEntity> findByGuest_Id(UUID guestId, Pageable pageable);
}
