package com.pingsdorf.server.ping;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pingsdorf.server.auth.CurrentUser;
import com.pingsdorf.server.ping.PingDtos.CreateRequest;
import com.pingsdorf.server.ping.PingDtos.ImageUrlResponse;
import com.pingsdorf.server.ping.PingDtos.PingView;
import com.pingsdorf.server.ping.PingDtos.UpdateRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pings")
public class PingController {

    private final PingService pings;
    private final CurrentUser currentUser;

    public PingController(PingService pings, CurrentUser currentUser) {
        this.pings = pings;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PingListResponse list() {
        var p = currentUser.require();
        return new PingListResponse(pings.list(p.householdId()));
    }

    @PostMapping
    public PingView create(@Valid @RequestBody CreateRequest req) {
        var p = currentUser.require();
        return pings.create(p.householdId(), p.userId(), req);
    }

    @PutMapping("/{id}")
    public PingView update(@PathVariable String id, @Valid @RequestBody UpdateRequest req) {
        var p = currentUser.require();
        return pings.update(p.householdId(), p.userId(), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        var p = currentUser.require();
        pings.delete(p.householdId(), p.userId(), id);
    }

    @PostMapping("/{id}/cleanup")
    public PingView cleanup(@PathVariable String id) {
        var p = currentUser.require();
        return pings.cleanup(p.householdId(), p.userId(), id);
    }

    @PostMapping("/{id}/image")
    public ImageUrlResponse uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        var p = currentUser.require();
        return new ImageUrlResponse(pings.uploadImage(p.householdId(), p.userId(), id, file));
    }

    public record PingListResponse(List<PingView> pings) {}
}
