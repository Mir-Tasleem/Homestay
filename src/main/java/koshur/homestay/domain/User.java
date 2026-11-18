package koshur.homestay.domain;

import java.util.Objects;
import java.util.UUID;

/** A homestay guest or host. Identity is immutable; profile data is encapsulated. */
public class User {
    private final UUID id;
    private final String email;
    private String displayName;

    public User(String email, String displayName) {
        this(UUID.randomUUID(), email, displayName);
    }

    public User(UUID id, String email, String displayName) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = requireText(email, "email");
        this.displayName = requireText(displayName, "displayName");
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }

    public void rename(String displayName) {
        this.displayName = requireText(displayName, "displayName");
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.strip();
    }
}
