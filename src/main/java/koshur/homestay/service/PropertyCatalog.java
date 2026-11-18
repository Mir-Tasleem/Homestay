package koshur.homestay.service;

import koshur.homestay.domain.Property;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/** Stream-based property search over an immutable snapshot of listings. */
public class PropertyCatalog {
    private final List<Property> properties;

    public PropertyCatalog(List<Property> properties) {
        this.properties = List.copyOf(properties);
    }

    public List<Property> filter(String city, Integer minimumGuests, BigDecimal maximumNightlyRate) {
        Stream<Property> matches = properties.stream();
        if (city != null && !city.isBlank()) {
            String requestedCity = city.strip();
            matches = matches.filter(property -> property.getCity().equalsIgnoreCase(requestedCity));
        }
        if (minimumGuests != null) {
            if (minimumGuests < 1) throw new IllegalArgumentException("minimumGuests must be positive");
            matches = matches.filter(property -> property.getMaxGuests() >= minimumGuests);
        }
        if (maximumNightlyRate != null) {
            if (maximumNightlyRate.signum() < 0) throw new IllegalArgumentException("maximumNightlyRate must not be negative");
            matches = matches.filter(property -> property.getNightlyRate().compareTo(maximumNightlyRate) <= 0);
        }
        return matches.toList();
    }
}
