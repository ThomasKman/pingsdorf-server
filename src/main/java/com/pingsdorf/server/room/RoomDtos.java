package com.pingsdorf.server.room;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class RoomDtos {

    private RoomDtos() {}

    private static final String HEX_COLOR = "^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$";

    public record PointDto(
            @DecimalMin("0.0") @DecimalMax("100.0") double x,
            @DecimalMin("0.0") @DecimalMax("100.0") double y
    ) {
        Room.Point toDomain() { return new Room.Point(x, y); }
        static PointDto of(Room.Point p) { return new PointDto(p.x(), p.y()); }
    }

    public record CreateRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Pattern(regexp = HEX_COLOR) String color,
            @NotNull @Size(min = 3, max = 200) List<@Valid PointDto> points
    ) {}

    public record UpdateRequest(
            @Size(max = 80) String name,
            @Pattern(regexp = HEX_COLOR) String color,
            @Size(min = 3, max = 200) List<@Valid PointDto> points
    ) {}

    public record RoomView(
            String id,
            String name,
            String color,
            List<PointDto> points,
            Instant createdAt
    ) {
        public static RoomView of(Room r) {
            return new RoomView(
                    r.getId(),
                    r.getName(),
                    r.getColor(),
                    r.getPoints().stream().map(PointDto::of).toList(),
                    r.getCreatedAt());
        }
    }
}
