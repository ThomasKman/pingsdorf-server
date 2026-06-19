package com.pingsdorf.server.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.household.Household;
import com.pingsdorf.server.household.HouseholdRepository;
import com.pingsdorf.server.user.User;
import com.pingsdorf.server.user.UserRepository;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private static final String DEFAULT_USER_COLOR = "#4a9eff";

    private final UserRepository users;
    private final HouseholdRepository households;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users,
                       HouseholdRepository households,
                       PasswordEncoder encoder,
                       JwtService jwt) {
        this.users = users;
        this.households = households;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Create a new user and a fresh household for them. */
    public AuthResult signup(String email, String password, String name) {
        var normalizedEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }
        var household = households.save(Household.builder()
                .mapRotation(0)
                .createdAt(Instant.now())
                .build());
        var user = users.save(User.builder()
                .email(normalizedEmail)
                .passwordHash(encoder.encode(password))
                .name(name)
                .color(DEFAULT_USER_COLOR)
                .householdId(household.getId())
                .createdAt(Instant.now())
                .build());
        return new AuthResult(user, household, jwt.issue(user.getId(), household.getId()));
    }

    public AuthResult login(String email, String password) {
        var user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }
        var household = households.findById(user.getHouseholdId())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Household missing"));
        return new AuthResult(user, household, jwt.issue(user.getId(), household.getId()));
    }

    public record AuthResult(User user, Household household, String token) {}
}
