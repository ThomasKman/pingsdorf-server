package com.pingsdorf.server.household;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class HouseholdDtos {

    private HouseholdDtos() {}

    public record UpdateRequest(
            @Size(max = 80) String name,
            @Min(0) @Max(359) Integer mapRotation
    ) {}

    public record InviteResponse(String inviteCode) {}

    public record JoinRequest(@NotBlank String inviteCode) {}

    public record FloorPlanResponse(String floorPlanUrl) {}

    public record HouseholdView(String id, String name, String floorPlanUrl, int mapRotation, String inviteCode) {
        public static HouseholdView of(Household h) {
            return new HouseholdView(h.getId(), h.getName(), h.getFloorPlanUrl(), h.getMapRotation(), h.getInviteCode());
        }
    }
}
