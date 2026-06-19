package com.pingsdorf.server.household;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface HouseholdRepository extends MongoRepository<Household, String> {

    Optional<Household> findByInviteCode(String inviteCode);
}
