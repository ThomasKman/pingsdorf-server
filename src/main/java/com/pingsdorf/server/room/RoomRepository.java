package com.pingsdorf.server.room;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomRepository extends MongoRepository<Room, String> {

    List<Room> findByHouseholdIdOrderByCreatedAtAsc(String householdId);
}
