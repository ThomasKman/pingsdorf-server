package com.pingsdorf.server.room;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pingsdorf.server.realtime.RealtimePublisher;
import com.pingsdorf.server.room.RoomDtos.CreateRequest;
import com.pingsdorf.server.room.RoomDtos.RoomView;
import com.pingsdorf.server.room.RoomDtos.UpdateRequest;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class RoomService {

    private final RoomRepository rooms;
    private final RealtimePublisher events;

    public RoomService(RoomRepository rooms, RealtimePublisher events) {
        this.rooms = rooms;
        this.events = events;
    }

    public List<Room> list(String householdId) {
        return rooms.findByHouseholdIdOrderByCreatedAtAsc(householdId);
    }

    public Room create(String householdId, CreateRequest req) {
        var room = rooms.save(Room.builder()
                .name(req.name())
                .color(req.color())
                .points(req.points().stream().map(RoomDtos.PointDto::toDomain).toList())
                .householdId(householdId)
                .createdAt(Instant.now())
                .build());
        events.publish(householdId, "room:created", RoomView.of(room));
        return room;
    }

    public Room update(String householdId, String id, UpdateRequest req) {
        var room = requireOwned(householdId, id);
        if (req.name() != null) room.setName(req.name());
        if (req.color() != null) room.setColor(req.color());
        if (req.points() != null) {
            room.setPoints(req.points().stream().map(RoomDtos.PointDto::toDomain).toList());
        }
        var saved = rooms.save(room);
        events.publish(householdId, "room:updated", RoomView.of(saved));
        return saved;
    }

    public void delete(String householdId, String id) {
        var room = requireOwned(householdId, id);
        rooms.delete(room);
        events.publish(householdId, "room:deleted", java.util.Map.of("id", id));
    }

    private Room requireOwned(String householdId, String id) {
        var room = rooms.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Room not found"));
        if (!householdId.equals(room.getHouseholdId())) {
            throw new ResponseStatusException(FORBIDDEN, "Room belongs to another household");
        }
        return room;
    }
}
