package koshur.homestay.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** A homestay listing with immutable identity and pricing details. */
public class Property {
    private final UUID id;
    private final UUID hostId;
    private final String name;
    private final String city;
    private final int maxGuests;
    private final BigDecimal nightlyRate;

    public Property(UUID hostId, String name, String city, int maxGuests, BigDecimal nightlyRate) {
        this(UUID.randomUUID(), hostId, name, city, maxGuests, nightlyRate);
    }

    public Property(UUID id, UUID hostId, String name, String city, int maxGuests, BigDecimal nightlyRate) {
        this.id = Objects.requireNonNull(id, "id");
        this.hostId = Objects.requireNonNull(hostId, "hostId");
        this.name = requireText(name, "name");
        this.city = requireText(city, "city");
        if (maxGuests < 1) throw new IllegalArgumentException("maxGuests must be positive");
        this.maxGuests = maxGuests;
        this.nightlyRate = Objects.requireNonNull(nightlyRate, "nightlyRate");
        if (nightlyRate.signum() < 0) throw new IllegalArgumentException("nightlyRate must not be negative");
    }

    public UUID getId() { return id; }
    public UUID getHostId() { return hostId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getMaxGuests() { return maxGuests; }
    public BigDecimal getNightlyRate() { return nightlyRate; }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.strip();
    }
}
