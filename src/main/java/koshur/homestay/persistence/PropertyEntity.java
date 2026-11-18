package koshur.homestay.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "properties", indexes = {@Index(name = "idx_properties_location", columnList = "location"),
        @Index(name = "idx_properties_owner", columnList = "owner_id")})
public class PropertyEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;
    @Column(nullable = false, length = 160) private String name;
    @Column(name = "description", nullable = false, length = 2000) private String description;
    @Column(name = "location", nullable = false, length = 160) private String city;
    @Column(name = "capacity", nullable = false) private int maxGuests;
    @Column(name = "price_per_night", nullable = false, precision = 12, scale = 2) private BigDecimal nightlyRate;
    @Version private long version;
    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY)
    private List<BookingEntity> bookings = new ArrayList<>();

    protected PropertyEntity() {}
    public PropertyEntity(AppUser owner, String name, String description, String city, int maxGuests, BigDecimal nightlyRate) {
        this.owner = owner; this.name = name; this.description = description; this.city = city;
        this.maxGuests = maxGuests; this.nightlyRate = nightlyRate;
    }
    public void update(String name, String description, String city, int maxGuests, BigDecimal nightlyRate) {
        this.name = name; this.description = description; this.city = city;
        this.maxGuests = maxGuests; this.nightlyRate = nightlyRate;
    }
    public UUID getId() { return id; }
    public AppUser getOwner() { return owner; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public int getMaxGuests() { return maxGuests; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
}
