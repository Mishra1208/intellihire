package com.intellihire.backend.auth;

import com.intellihire.backend.auth.dto.AuthResponse;
import com.intellihire.backend.auth.dto.LoginRequest;
import com.intellihire.backend.auth.dto.RegisterRequest;
import com.intellihire.backend.user.User;
import com.intellihire.backend.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin( // extra safety in case global CORS is changed
        origins = "${app.cors.allowedOrigins:http://localhost:5173}",
        allowCredentials = "true"
)
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.session.user-key:USER_ID}")
    private String userSessionKey;

    // --- helpers -------------------------------------------------------------

    private static String normEmail(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private static AuthResponse toAuthResponse(User u) {
        return new AuthResponse(u.getId(), u.getName(), u.getEmail());
    }

    private void putUserInSession(HttpSession session, Long userId) {
        session.setAttribute(userSessionKey, userId);
    }

    // --- endpoints -----------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        final String name = req.name().trim();
        final String email = normEmail(req.email());
        final String password = req.password();

        // duplicate email check (normalize before checking)
        if (users.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of("message", "Email already in use"));
        }

        // (Optional) lightweight policy check
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }

        User u = User.builder()
                .name(name)
                .email(email)
                .passwordHash(encoder.encode(password))
                .build();

        users.save(u);
        putUserInSession(session, u.getId());

        // 201 Created with the new resource representation
        return ResponseEntity.created(URI.create("/api/auth/me")).body(toAuthResponse(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        final String email = normEmail(req.email());
        final String password = req.password();

        User u = users.findByEmail(email).orElse(null);
        if (u == null || !encoder.matches(password, u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }

        putUserInSession(session, u.getId());
        return ResponseEntity.ok(toAuthResponse(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object id = session.getAttribute(userSessionKey);
        if (id == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        User u = users.findById((Long) id).orElse(null);
        if (u == null) {
            // stale session; clear it
            session.invalidate();
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        return ResponseEntity.ok(toAuthResponse(u));
    }
}
