package com.pingsdorf.server.room;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pingsdorf.server.auth.CurrentUser;
import com.pingsdorf.server.room.RoomDtos.CreateRequest;
import com.pingsdorf.server.room.RoomDtos.RoomView;
import com.pingsdorf.server.room.RoomDtos.UpdateRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService rooms;
    private final CurrentUser currentUser;

    public RoomController(RoomService rooms, CurrentUser currentUser) {
        this.rooms = rooms;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<RoomView> list() {
        return rooms.list(currentUser.require().householdId()).stream().map(RoomView::of).toList();
    }

    @PostMapping
    public RoomView create(@Valid @RequestBody CreateRequest req) {
        return RoomView.of(rooms.create(currentUser.require().householdId(), req));
    }

    @PutMapping("/{id}")
    public RoomView update(@PathVariable String id, @Valid @RequestBody UpdateRequest req) {
        return RoomView.of(rooms.update(currentUser.require().householdId(), id, req));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        rooms.delete(currentUser.require().householdId(), id);
    }
}
