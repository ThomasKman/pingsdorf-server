package com.pingsdorf.server.auth;

import com.pingsdorf.server.household.Household;
import com.pingsdorf.server.user.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request/response payloads for {@link AuthController}. */
public final class AuthDtos {

    private AuthDtos() {}

    public record SignupRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 200) String password,
            @NotBlank @Size(max = 80) String name
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(String token, UserView user, HouseholdView household) {
        public static AuthResponse of(AuthService.AuthResult r) {
            return new AuthResponse(r.token(), UserView.of(r.user()), HouseholdView.of(r.household()));
        }
    }

    public record MeResponse(UserView user, HouseholdView household) {
        public static MeResponse of(User u, Household h) {
            return new MeResponse(UserView.of(u), HouseholdView.of(h));
        }
    }

    public record UserView(String id, String email, String name, String color, String avatar) {
        public static UserView of(User u) {
            return new UserView(u.getId(), u.getEmail(), u.getName(), u.getColor(), u.getAvatar());
        }
    }

    public record HouseholdView(String id, String name, String floorPlanUrl, int mapRotation, String inviteCode) {
        public static HouseholdView of(Household h) {
            return new HouseholdView(h.getId(), h.getName(), h.getFloorPlanUrl(), h.getMapRotation(), h.getInviteCode());
        }
    }
}
