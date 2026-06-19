package com.pingsdorf.server.household;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.auth.CurrentUser;
import com.pingsdorf.server.household.HouseholdDtos.FloorPlanResponse;
import com.pingsdorf.server.household.HouseholdDtos.HouseholdView;
import com.pingsdorf.server.household.HouseholdDtos.InviteResponse;
import com.pingsdorf.server.household.HouseholdDtos.JoinRequest;
import com.pingsdorf.server.household.HouseholdDtos.UpdateRequest;
import com.pingsdorf.server.user.UserRepository;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/household")
public class HouseholdController {

    private final HouseholdService households;
    private final CurrentUser currentUser;
    private final UserRepository users;

    public HouseholdController(HouseholdService households, CurrentUser currentUser, UserRepository users) {
        this.households = households;
        this.currentUser = currentUser;
        this.users = users;
    }

    @PostMapping("/invite")
    public InviteResponse generateInvite() {
        var code = households.generateInviteCode(currentUser.require().householdId());
        return new InviteResponse(code);
    }

    @PostMapping("/join")
    public HouseholdView join(@Valid @RequestBody JoinRequest req) {
        var p = currentUser.require();
        var caller = users.findById(p.userId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        var joined = households.joinByCode(caller, req.inviteCode());
        return HouseholdView.of(joined);
    }

    @PutMapping
    public HouseholdView update(@Valid @RequestBody UpdateRequest req) {
        var updated = households.update(currentUser.require().householdId(), req);
        return HouseholdView.of(updated);
    }

    @PostMapping("/floor-plan")
    public FloorPlanResponse uploadFloorPlan(@RequestParam("file") MultipartFile file) {
        var url = households.uploadFloorPlan(currentUser.require().householdId(), file);
        return new FloorPlanResponse(url);
    }
}
