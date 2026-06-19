package com.pingsdorf.server.ping;

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
@Document(collection = "pings")
public class Ping {

    @Id
    private String id;

    private String name;

    private String description;

    private String imageUrl;

    /** X coordinate as percentage 0..100. */
    private double x;

    /** Y coordinate as percentage 0..100. */
    private double y;

    private String roomId;

    @Indexed
    private String householdId;

    private String createdByUserId;

    private Instant createdAt;

    private String cleanedUpByUserId;

    private Instant cleanedUpAt;
}
