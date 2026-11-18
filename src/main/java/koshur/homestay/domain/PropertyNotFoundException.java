package koshur.homestay.domain;

import java.util.UUID;

public final class PropertyNotFoundException extends BookingException {
    public PropertyNotFoundException(UUID propertyId) {
        super("Property not found: " + propertyId);
    }
}
