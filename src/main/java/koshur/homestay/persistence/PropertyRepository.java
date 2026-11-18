package koshur.homestay.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    Page<PropertyEntity> findByCityContainingIgnoreCase(String city, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PropertyEntity p where p.id = :id")
    Optional<PropertyEntity> findByIdForUpdate(@Param("id") UUID id);
}
