CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'HOST'))
);

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(160) NOT NULL,
    price_per_night NUMERIC(12, 2) NOT NULL,
    capacity INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT ck_properties_price CHECK (price_per_night >= 0),
    CONSTRAINT ck_properties_capacity CHECK (capacity > 0)
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    property_id UUID NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    guest_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_property FOREIGN KEY (property_id) REFERENCES properties (id),
    CONSTRAINT ck_bookings_dates CHECK (check_out > check_in),
    CONSTRAINT ck_bookings_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_bookings_guest_count CHECK (guest_count > 0),
    CONSTRAINT ck_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

CREATE INDEX idx_properties_location ON properties (location);
CREATE INDEX idx_properties_owner ON properties (owner_id);
CREATE INDEX idx_bookings_property_dates_status ON bookings (property_id, check_in, check_out, status);
CREATE INDEX idx_bookings_user ON bookings (user_id);
