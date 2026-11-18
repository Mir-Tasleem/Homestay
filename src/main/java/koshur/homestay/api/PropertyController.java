package koshur.homestay.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import koshur.homestay.api.dto.PropertyRequest;
import koshur.homestay.api.dto.PropertyResponse;
import koshur.homestay.service.PropertyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Validated
public class PropertyController {
    private final PropertyService properties;
    public PropertyController(PropertyService properties) { this.properties = properties; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody PropertyRequest request) {
        return properties.create(userId, request);
    }

    @GetMapping
    public Page<PropertyResponse> search(@RequestParam(required = false) String city,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return properties.search(city, page, size);
    }

    @GetMapping("/{id}")
    public PropertyResponse get(@PathVariable UUID id) { return properties.get(id); }

    @PutMapping("/{id}")
    public PropertyResponse update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                   @Valid @RequestBody PropertyRequest request) {
        return properties.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) { properties.delete(userId, id); }
}
