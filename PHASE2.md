# Phase 2 API

The backend uses Spring MVC, Spring Data JPA, Hibernate, PostgreSQL, Flyway migrations, HikariCP, and JWT bearer tokens. Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` to connect another database. Hibernate validates the schema created by Flyway.

`JWT_SECRET` is required at startup and must contain at least 32 UTF-8 bytes. For local development, generate one with `openssl rand -base64 48` and export it before starting the app. Do not commit the secret. Access tokens expire after one hour by default (`app.jwt.expiration`).

## Start

```sh
docker compose up -d postgres
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

The Compose file starts a local development database. Change its credentials before using a non-local deployment. Run tests with `./mvnw test`; PostgreSQL integration tests use Testcontainers and skip when Docker is unavailable.

## Routes

| Method | Route | Access |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/users/me` | Bearer token |
| POST | `/api/v1/properties` | Bearer token; creates a listing owned by the caller |
| GET | `/api/v1/properties?page=0&size=20&city=Delhi` | Public, paginated |
| GET | `/api/v1/properties/{id}` | Public |
| PUT | `/api/v1/properties/{id}` | Owner only |
| DELETE | `/api/v1/properties/{id}` | Owner only; only if it has no bookings |
| POST | `/api/v1/bookings` | Bearer token |
| GET | `/api/v1/bookings/{id}` | Booking guest only |
| GET | `/api/v1/bookings/my-bookings?page=0&size=20` | Bearer token |
| PUT | `/api/v1/bookings/{id}/cancel` | Booking guest only |

Property create/update request bodies include `name`, `description`, `city`, `maxGuests`, and `nightlyRate`. Registration and login return an access token. Send it as `Authorization: Bearer <accessToken>` to protected routes. Error responses use JSON with `timestamp`, `status`, `error`, and `message`. Health is available at `/actuator/health`.

## Booking consistency

The booking transaction obtains a pessimistic write lock on the property row before checking confirmed bookings and inserting the new booking. Concurrent attempts for the same property therefore perform the overlap check in sequence; the later transaction receives HTTP 409 if the dates overlap. Date intervals are half-open: check-in is included and check-out is excluded, so a booking can start on another booking's check-out date.
