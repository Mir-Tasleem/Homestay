package koshur.homestay;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import koshur.homestay.api.ApiException;
import koshur.homestay.api.dto.BookingRequest;
import koshur.homestay.api.dto.BookingResponse;
import koshur.homestay.persistence.*;
import koshur.homestay.service.PersistentBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.jwt.secret=integration-test-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class HomestayApplicationTests {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PropertyRepository properties;
    @Autowired PersistentBookingService bookingService;

    @BeforeEach
    void clearDatabase() {
        jdbc.update("DELETE FROM bookings");
        jdbc.update("DELETE FROM properties");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void contextLoadsAndFlywayCreatesPostgresSchema() {
        assertEquals(3, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name IN ('users', 'properties', 'bookings')", Integer.class));
    }

    @Test
    void apiRegistersUserCreatesPropertyAndBooksIt() throws Exception {
        String register = mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                        .content("""
                                {"email":"host@example.com","password":"StrongPass123!","displayName":"Host"}
                                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String hostToken = json.readTree(register).path("accessToken").asText();
        String propertyJson = mvc.perform(post("/api/v1/properties").header("Authorization", "Bearer " + hostToken)
                        .contentType("application/json").content("""
                                {"name":"Lake House","description":"A quiet stay","city":"Srinagar","maxGuests":4,"nightlyRate":125.00}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String propertyId = json.readTree(propertyJson).path("id").asText();
        String guestResponse = mvc.perform(post("/api/v1/auth/register").contentType("application/json")
                        .content("""
                                {"email":"guest@example.com","password":"StrongPass123!","displayName":"Guest"}
                                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String guestToken = json.readTree(guestResponse).path("accessToken").asText();
        LocalDate start = LocalDate.now().plusDays(20);
        String bookingRequest = """ 
                {"propertyId":"%s","checkIn":"%s","checkOut":"%s","guestCount":2}
                """.formatted(propertyId, start, start.plusDays(2));

        String bookingJson = mvc.perform(post("/api/v1/bookings").header("Authorization", "Bearer " + guestToken)
                        .contentType("application/json").content(bookingRequest))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode booking = json.readTree(bookingJson);
        assertEquals("250.00", booking.path("totalAmount").decimalValue().toPlainString());
        mvc.perform(get("/api/v1/bookings/" + booking.path("id").asText())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isOk());
    }

    @Test
    void concurrentOverlappingBookingsAllowOnlyOneWinner() throws Exception {
        AppUser owner = saveUser("owner@example.com");
        AppUser guestOne = saveUser("one@example.com");
        AppUser guestTwo = saveUser("two@example.com");
        PropertyEntity property = properties.saveAndFlush(new PropertyEntity(owner, "Cabin", "Near the lake",
                "Srinagar", 4, new BigDecimal("90.00")));
        LocalDate start = LocalDate.now().plusDays(30);
        BookingRequest firstRequest = new BookingRequest(property.getId(), start, start.plusDays(3), 2);
        BookingRequest secondRequest = new BookingRequest(property.getId(), start.plusDays(1), start.plusDays(4), 2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startTogether = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Object> first = pool.submit(attempt(guestOne.getId(), firstRequest, ready, startTogether));
            Future<Object> second = pool.submit(attempt(guestTwo.getId(), secondRequest, ready, startTogether));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            startTogether.countDown();
            Object resultOne = first.get(15, TimeUnit.SECONDS);
            Object resultTwo = second.get(15, TimeUnit.SECONDS);
            assertEquals(1, (resultOne instanceof BookingResponse ? 1 : 0) + (resultTwo instanceof BookingResponse ? 1 : 0));
            Object rejected = resultOne instanceof ApiException ? resultOne : resultTwo;
            assertInstanceOf(ApiException.class, rejected);
            assertEquals(409, ((ApiException) rejected).getStatus().value());
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM bookings WHERE status = 'CONFIRMED'", Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }

    private Callable<Object> attempt(UUID guestId, BookingRequest request, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await();
            try { return bookingService.create(guestId, request); }
            catch (ApiException exception) { return exception; }
        };
    }

    private AppUser saveUser(String email) {
        return users.saveAndFlush(new AppUser(email, email, "not-used-by-test"));
    }
}
