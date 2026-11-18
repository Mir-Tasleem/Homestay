package koshur.homestay.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 320) private String email;
    @Column(name = "name", nullable = false, length = 120) private String displayName;
    @Column(name = "password", nullable = false, length = 100) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role = Role.USER;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY) private List<PropertyEntity> properties = new ArrayList<>();
    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY) private List<BookingEntity> bookings = new ArrayList<>();

    public enum Role { USER, HOST }

    protected AppUser() {}
    public AppUser(String email, String displayName, String passwordHash) {
        this.email = email; this.displayName = displayName; this.passwordHash = passwordHash;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
