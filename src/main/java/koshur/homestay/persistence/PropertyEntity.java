package koshur.homestay.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "properties", indexes = {@Index(name = "idx_properties_city", columnList = "city"),
        @Index(name = "idx_properties_owner", columnList = "owner_id")})
public class PropertyEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;
    @Column(nullable = false, length = 160) private String name;
    @Column(nullable = false, length = 120) private String city;
    @Column(name = "max_guests", nullable = false) private int maxGuests;
    @Column(name = "nightly_rate", nullable = false, precision = 12, scale = 2) private BigDecimal nightlyRate;
    @Version private long version;

    protected PropertyEntity() {}
    public PropertyEntity(AppUser owner, String name, String city, int maxGuests, BigDecimal nightlyRate) {
        this.owner = owner; this.name = name; this.city = city;
        this.maxGuests = maxGuests; this.nightlyRate = nightlyRate;
    }
    public void update(String name, String city, int maxGuests, BigDecimal nightlyRate) {
        this.name = name; this.city = city; this.maxGuests = maxGuests; this.nightlyRate = nightlyRate;
    }
    public UUID getId() { return id; }
    public AppUser getOwner() { return owner; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getMaxGuests() { return maxGuests; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
}
