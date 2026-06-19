package com.pingsdorf.server.ping;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PingRepository extends MongoRepository<Ping, String> {

    List<Ping> findByHouseholdIdOrderByCreatedAtDesc(String householdId);
}
