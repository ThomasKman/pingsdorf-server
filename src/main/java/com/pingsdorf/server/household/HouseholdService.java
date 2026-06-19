package com.pingsdorf.server.household;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.household.HouseholdDtos.UpdateRequest;
import com.pingsdorf.server.realtime.RealtimePublisher;
import com.pingsdorf.server.storage.StorageService;
import com.pingsdorf.server.user.User;
import com.pingsdorf.server.user.UserRepository;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class HouseholdService {

    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LENGTH = 8;
    private static final int MAX_INVITE_ATTEMPTS = 8;

    private final HouseholdRepository households;
    private final UserRepository users;
    private final StorageService storage;
    private final RealtimePublisher events;
    private final SecureRandom random = new SecureRandom();

    public HouseholdService(HouseholdRepository households,
                            UserRepository users,
                            StorageService storage,
                            RealtimePublisher events) {
        this.households = households;
        this.users = users;
        this.storage = storage;
        this.events = events;
    }

    public Household get(String id) {
        return households.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Household not found"));
    }

    public Household update(String householdId, UpdateRequest req) {
        var h = get(householdId);
        if (req.name() != null) h.setName(req.name());
        if (req.mapRotation() != null) h.setMapRotation(req.mapRotation());
        var saved = households.save(h);
        events.publish(householdId, "household:updated", HouseholdDtos.HouseholdView.of(saved));
        return saved;
    }

    public String generateInviteCode(String householdId) {
        var h = get(householdId);
        for (int i = 0; i < MAX_INVITE_ATTEMPTS; i++) {
            var code = randomCode();
            if (households.findByInviteCode(code).isEmpty()) {
                h.setInviteCode(code);
                households.save(h);
                return code;
            }
        }
        throw new IllegalStateException("Unable to allocate unique invite code");
    }

    /**
     * Move the calling user into the household identified by {@code inviteCode}.
     * The previous (auto-created) household is deleted if it becomes empty.
     */
    public Household joinByCode(User caller, String inviteCode) {
        var target = households.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Invalid invite code"));
        if (target.getId().equals(caller.getHouseholdId())) {
            return target;
        }
        var previousHouseholdId = caller.getHouseholdId();
        caller.setHouseholdId(target.getId());
        users.save(caller);

        if (previousHouseholdId != null && users.findByHouseholdId(previousHouseholdId).isEmpty()) {
            households.deleteById(previousHouseholdId);
        }
        return target;
    }

    public String uploadFloorPlan(String householdId, MultipartFile file) {
        var h = get(householdId);
        var stored = storage.store("floor-plans", file);
        h.setFloorPlanUrl(stored.url());
        var saved = households.save(h);
        events.publish(householdId, "household:updated", HouseholdDtos.HouseholdView.of(saved));
        return stored.url();
    }

    private String randomCode() {
        var sb = new StringBuilder(INVITE_LENGTH);
        for (int i = 0; i < INVITE_LENGTH; i++) {
            sb.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
