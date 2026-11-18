package koshur.homestay.service;

import koshur.homestay.api.ApiException;
import koshur.homestay.api.dto.PropertyRequest;
import koshur.homestay.persistence.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {
    @Mock PropertyRepository properties;
    @Mock UserRepository users;
    @Mock BookingRepository bookings;

    @Test
    void anotherUsersPropertyCannotBeUpdated() {
        UUID propertyId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        AppUser owner = mock(AppUser.class);
        PropertyEntity property = mock(PropertyEntity.class);
        when(properties.findById(propertyId)).thenReturn(Optional.of(property));
        when(property.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(UUID.randomUUID());
        var request = new PropertyRequest("New name", "A description", "Srinagar", 2, new BigDecimal("75.00"));

        PropertyService service = new PropertyService(properties, users, bookings);
        ApiException error = assertThrows(ApiException.class, () -> service.update(callerId, propertyId, request));

        assertEquals(403, error.getStatus().value());
        verify(property, never()).update(anyString(), anyString(), anyString(), anyInt(), any());
    }
}
