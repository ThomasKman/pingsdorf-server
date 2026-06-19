package com.pingsdorf.server.ping;

import java.time.Instant;

import com.pingsdorf.server.user.User;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PingDtos {

    private PingDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 1000) String description,
            String imageUrl,
            @DecimalMin("0.0") @DecimalMax("100.0") double x,
            @DecimalMin("0.0") @DecimalMax("100.0") double y
    ) {}

    public record UpdateRequest(
            @Size(max = 120) String name,
            @Size(max = 1000) String description,
            String imageUrl,
            @DecimalMin("0.0") @DecimalMax("100.0") Double x,
            @DecimalMin("0.0") @DecimalMax("100.0") Double y
    ) {}

    public record ImageUrlResponse(String imageUrl) {}

    public record UserSummary(String id, String name, String color) {
        public static UserSummary of(User u) {
            return u == null ? null : new UserSummary(u.getId(), u.getName(), u.getColor());
        }
    }

    public record PingView(
            String id,
            String name,
            String description,
            String imageUrl,
            double x,
            double y,
            String roomId,
            UserSummary createdBy,
            Instant createdAt,
            UserSummary cleanedUpBy,
            Instant cleanedUpAt
    ) {
        public static PingView of(Ping p, UserSummary createdBy, UserSummary cleanedUpBy) {
            return new PingView(
                    p.getId(), p.getName(), p.getDescription(), p.getImageUrl(),
                    p.getX(), p.getY(), p.getRoomId(),
                    createdBy, p.getCreatedAt(),
                    cleanedUpBy, p.getCleanedUpAt());
        }
    }
}
