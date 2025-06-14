package com.running.repository;

import com.running.model.UserRace;
import com.running.model.UserRaceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRaceRepository extends JpaRepository<UserRace, UserRaceId> {
    List<UserRace> findByUser_UID(String uid);
    Optional<UserRace> findByUserIdAndRaceId(Long userId, Long raceId);

}

