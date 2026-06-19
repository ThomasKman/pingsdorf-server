package com.pingsdorf.server.room;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    private String name;

    /** Hex color (e.g. {@code #4a9eff}). */
    private String color;

    /** Polygon vertices, each coordinate is a percentage 0..100. */
    private List<Point> points;

    @Indexed
    private String householdId;

    private Instant createdAt;

    public record Point(double x, double y) {}
}
