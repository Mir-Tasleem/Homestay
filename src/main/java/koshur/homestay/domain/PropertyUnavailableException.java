package koshur.homestay.domain;

import java.util.UUID;

public final class PropertyUnavailableException extends BookingException {
    public PropertyUnavailableException(UUID propertyId) {
        super("Property is unavailable for the requested dates: " + propertyId);
    }
}
