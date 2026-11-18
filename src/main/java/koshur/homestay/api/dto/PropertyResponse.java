package koshur.homestay.api.dto;

import koshur.homestay.persistence.PropertyEntity;
import java.math.BigDecimal;
import java.util.UUID;

public record PropertyResponse(UUID id, UUID ownerId, String name, String description, String city,
                               int maxGuests, BigDecimal nightlyRate) {
    public static PropertyResponse from(PropertyEntity property) {
        return new PropertyResponse(property.getId(), property.getOwner().getId(), property.getName(), property.getDescription(),
                property.getCity(), property.getMaxGuests(), property.getNightlyRate());
    }
}
