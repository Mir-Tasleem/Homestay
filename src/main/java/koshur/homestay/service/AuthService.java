package koshur.homestay.service;

import koshur.homestay.api.ApiException;
import koshur.homestay.api.dto.*;
import koshur.homestay.persistence.AppUser;
import koshur.homestay.persistence.UserRepository;
import koshur.homestay.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService tokens) {
        this.users = users; this.passwords = passwords; this.tokens = tokens;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        AppUser user = users.save(new AppUser(email, request.displayName().strip(), passwords.encode(request.password())));
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = users.findByEmailIgnoreCase(request.email().strip())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwords.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(java.util.UUID userId) {
        return UserResponse.from(users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User account no longer exists")));
    }

    private TokenResponse tokenResponse(AppUser user) {
        return new TokenResponse(tokens.createToken(user.getId()), "Bearer", tokens.lifetimeSeconds(),
                user.getId(), user.getEmail(), user.getDisplayName());
    }
}
