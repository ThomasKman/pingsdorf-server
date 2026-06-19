package com.pingsdorf.server.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.auth.AuthDtos.AuthResponse;
import com.pingsdorf.server.auth.AuthDtos.LoginRequest;
import com.pingsdorf.server.auth.AuthDtos.MeResponse;
import com.pingsdorf.server.auth.AuthDtos.SignupRequest;
import com.pingsdorf.server.household.HouseholdRepository;
import com.pingsdorf.server.user.UserRepository;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final UserRepository users;
    private final HouseholdRepository households;

    public AuthController(AuthService authService,
                          CurrentUser currentUser,
                          UserRepository users,
                          HouseholdRepository households) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.users = users;
        this.households = households;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest req) {
        return AuthResponse.of(authService.signup(req.email(), req.password(), req.name()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return AuthResponse.of(authService.login(req.email(), req.password()));
    }

    /**
     * Stateless logout — the client should discard its token. We keep the endpoint
     * for API symmetry and to make future token-blacklist support trivial to add.
     */
    @PostMapping("/logout")
    public void logout() {
        // no-op
    }

    @GetMapping("/me")
    public MeResponse me() {
        var p = currentUser.require();
        var user = users.findById(p.userId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        var household = households.findById(p.householdId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Household not found"));
        return MeResponse.of(user, household);
    }
}
