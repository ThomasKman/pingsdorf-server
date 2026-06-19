package com.pingsdorf.server.ping;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.ping.PingDtos.CreateRequest;
import com.pingsdorf.server.ping.PingDtos.PingView;
import com.pingsdorf.server.ping.PingDtos.UpdateRequest;
import com.pingsdorf.server.ping.PingDtos.UserSummary;
import com.pingsdorf.server.realtime.RealtimePublisher;
import com.pingsdorf.server.room.RoomRepository;
import com.pingsdorf.server.storage.StorageService;
import com.pingsdorf.server.user.User;
import com.pingsdorf.server.user.UserRepository;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PingService {

    private final PingRepository pings;
    private final RoomRepository rooms;
    private final UserRepository users;
    private final StorageService storage;
    private final RealtimePublisher events;

    public PingService(PingRepository pings,
                       RoomRepository rooms,
                       UserRepository users,
                       StorageService storage,
                       RealtimePublisher events) {
        this.pings = pings;
        this.rooms = rooms;
        this.users = users;
        this.storage = storage;
        this.events = events;
    }

    public List<PingView> list(String householdId) {
        var raw = pings.findByHouseholdIdOrderByCreatedAtDesc(householdId);
        var userIds = raw.stream()
                .flatMap(p -> java.util.stream.Stream.of(p.getCreatedByUserId(), p.getCleanedUpByUserId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        var userMap = users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return raw.stream().map(p -> toView(p, userMap)).toList();
    }

    public PingView create(String householdId, String userId, CreateRequest req) {
        var roomList = rooms.findByHouseholdIdOrderByCreatedAtAsc(householdId);
        var roomId = RoomGeometry.findRoomContaining(roomList, req.x(), req.y());
        var saved = pings.save(Ping.builder()
                .name(req.name())
                .description(req.description() == null ? "" : req.description())
                .imageUrl(req.imageUrl())
                .x(req.x())
                .y(req.y())
                .roomId(roomId)
                .householdId(householdId)
                .createdByUserId(userId)
                .createdAt(Instant.now())
                .build());
        var view = toView(saved, loadUserMap(saved));
        events.publish(householdId, "ping:created", view);
        return view;
    }

    public PingView update(String householdId, String userId, String id, UpdateRequest req) {
        var ping = requireOwned(householdId, id);
        if (!ping.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only the creator can edit this ping");
        }
        if (req.name() != null) ping.setName(req.name());
        if (req.description() != null) ping.setDescription(req.description());
        if (req.imageUrl() != null) ping.setImageUrl(req.imageUrl());
        boolean moved = false;
        if (req.x() != null) { ping.setX(req.x()); moved = true; }
        if (req.y() != null) { ping.setY(req.y()); moved = true; }
        if (moved) {
            var roomList = rooms.findByHouseholdIdOrderByCreatedAtAsc(householdId);
            ping.setRoomId(RoomGeometry.findRoomContaining(roomList, ping.getX(), ping.getY()));
        }
        var saved = pings.save(ping);
        var view = toView(saved, loadUserMap(saved));
        events.publish(householdId, "ping:updated", view);
        return view;
    }

    public void delete(String householdId, String userId, String id) {
        var ping = requireOwned(householdId, id);
        if (!ping.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only the creator can delete this ping");
        }
        pings.delete(ping);
        events.publish(householdId, "ping:deleted", Map.of("id", id));
    }

    public PingView cleanup(String householdId, String userId, String id) {
        var ping = requireOwned(householdId, id);
        if (ping.getCleanedUpAt() == null) {
            ping.setCleanedUpByUserId(userId);
            ping.setCleanedUpAt(Instant.now());
            ping = pings.save(ping);
        }
        var view = toView(ping, loadUserMap(ping));
        events.publish(householdId, "ping:cleaned", view);
        return view;
    }

    public String uploadImage(String householdId, String userId, String id, MultipartFile file) {
        var ping = requireOwned(householdId, id);
        if (!ping.getCreatedByUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Only the creator can upload images");
        }
        var stored = storage.store("pings", file);
        ping.setImageUrl(stored.url());
        var saved = pings.save(ping);
        events.publish(householdId, "ping:updated", toView(saved, loadUserMap(saved)));
        return stored.url();
    }

    private Ping requireOwned(String householdId, String id) {
        var ping = pings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ping not found"));
        if (!householdId.equals(ping.getHouseholdId())) {
            throw new ResponseStatusException(FORBIDDEN, "Ping belongs to another household");
        }
        return ping;
    }

    private Map<String, User> loadUserMap(Ping ping) {
        var ids = new java.util.HashSet<String>();
        if (ping.getCreatedByUserId() != null) ids.add(ping.getCreatedByUserId());
        if (ping.getCleanedUpByUserId() != null) ids.add(ping.getCleanedUpByUserId());
        var map = new HashMap<String, User>();
        users.findAllById(ids).forEach(u -> map.put(u.getId(), u));
        return map;
    }

    private PingView toView(Ping p, Map<String, User> userMap) {
        var createdBy = UserSummary.of(userMap.get(p.getCreatedByUserId()));
        var cleanedUpBy = p.getCleanedUpByUserId() == null ? null : UserSummary.of(userMap.get(p.getCleanedUpByUserId()));
        return PingView.of(p, createdBy, cleanedUpBy);
    }
}
