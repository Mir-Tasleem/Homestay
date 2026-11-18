package koshur.homestay.service;

import koshur.homestay.api.ApiException;
import koshur.homestay.api.dto.PropertyRequest;
import koshur.homestay.api.dto.PropertyResponse;
import koshur.homestay.persistence.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PropertyService {
    private final PropertyRepository properties;
    private final UserRepository users;
    private final BookingRepository bookings;

    public PropertyService(PropertyRepository properties, UserRepository users, BookingRepository bookings) {
        this.properties = properties; this.users = users; this.bookings = bookings;
    }

    @Transactional
    public PropertyResponse create(UUID userId, PropertyRequest request) {
        AppUser owner = user(userId);
        return PropertyResponse.from(properties.save(new PropertyEntity(owner, clean(request.name()),
                clean(request.description()), clean(request.city()), request.maxGuests(), request.nightlyRate())));
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> search(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<PropertyEntity> result = city == null || city.isBlank()
                ? properties.findAll(pageable) : properties.findByCityContainingIgnoreCase(city.strip(), pageable);
        return result.map(PropertyResponse::from);
    }

    @Transactional(readOnly = true)
    public PropertyResponse get(UUID id) { return PropertyResponse.from(property(id)); }

    @Transactional
    public PropertyResponse update(UUID userId, UUID id, PropertyRequest request) {
        PropertyEntity property = property(id);
        requireOwner(userId, property);
        property.update(clean(request.name()), clean(request.description()), clean(request.city()),
                request.maxGuests(), request.nightlyRate());
        return PropertyResponse.from(property);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        PropertyEntity property = property(id);
        requireOwner(userId, property);
        if (bookings.existsByProperty_Id(id)) throw new ApiException(HttpStatus.CONFLICT, "Property has bookings and cannot be deleted");
        properties.delete(property);
    }

    private PropertyEntity property(UUID id) {
        return properties.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Property not found"));
    }
    private AppUser user(UUID id) {
        return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User account no longer exists"));
    }
    private static void requireOwner(UUID userId, PropertyEntity property) {
        if (!property.getOwner().getId().equals(userId)) throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this property");
    }
    private static String clean(String value) { return value.strip(); }
}
