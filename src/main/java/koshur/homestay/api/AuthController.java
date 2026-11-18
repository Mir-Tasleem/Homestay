package koshur.homestay.api;

import jakarta.validation.Valid;
import koshur.homestay.api.dto.*;
import koshur.homestay.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }

    @GetMapping("/users/me")
    public UserResponse me(@AuthenticationPrincipal UUID userId) { return auth.me(userId); }
}
