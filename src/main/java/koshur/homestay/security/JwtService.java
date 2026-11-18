package koshur.homestay.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final byte[] secret;
    private final Duration lifetime;
    private final JsonMapper mapper;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration:PT1H}") Duration lifetime,
                      JsonMapper mapper) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        if (lifetime.isNegative() || lifetime.isZero()) throw new IllegalArgumentException("JWT expiration must be positive");
        this.lifetime = lifetime;
        this.mapper = mapper;
    }

    public String createToken(UUID userId) {
        try {
            String header = ENCODER.encodeToString(mapper.writeValueAsBytes(mapper.createObjectNode()
                    .put("alg", "HS256").put("typ", "JWT")));
            String payload = ENCODER.encodeToString(mapper.writeValueAsBytes(mapper.createObjectNode()
                    .put("sub", userId.toString()).put("exp", Instant.now().plus(lifetime).getEpochSecond())));
            String unsigned = header + "." + payload;
            return unsigned + "." + ENCODER.encodeToString(sign(unsigned));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create access token", exception);
        }
    }

    public UUID parseUserId(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) throw new IllegalArgumentException("Malformed token");
            JsonNode header = mapper.readTree(DECODER.decode(parts[0]));
            if (!"HS256".equals(header.path("alg").asText())) throw new IllegalArgumentException("Unsupported algorithm");
            byte[] actual = DECODER.decode(parts[2]);
            if (!java.security.MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), actual)) {
                throw new IllegalArgumentException("Invalid token signature");
            }
            JsonNode payload = mapper.readTree(DECODER.decode(parts[1]));
            if (payload.path("exp").asLong(0) <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Expired token");
            }
            return UUID.fromString(payload.path("sub").asText());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid access token", exception);
        }
    }

    public long lifetimeSeconds() { return lifetime.toSeconds(); }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
    }
}
