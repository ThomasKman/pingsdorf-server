package com.pingsdorf.server.household;

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
@Document(collection = "households")
public class Household {

    @Id
    private String id;

    private String name;

    private String floorPlanUrl;

    /** 0 or 90 degrees. */
    private int mapRotation;

    @Indexed(unique = true, sparse = true)
    private String inviteCode;

    private Instant createdAt;
}
