package com.pingsdorf.server.user;

import java.time.Instant;

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
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String name;

    /** Hex color (e.g. {@code #4a9eff}) used for this user's pings on the map. */
    private String color;

    /** Optional emoji or image URL. */
    private String avatar;

    @Indexed
    private String householdId;

    private Instant createdAt;
}
