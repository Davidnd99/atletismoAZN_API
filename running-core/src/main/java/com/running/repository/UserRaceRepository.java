package com.running.repository;

import com.running.model.Career;
import com.running.model.User;
import com.running.model.UserRace;
import com.running.model.UserRaceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRaceRepository extends JpaRepository<UserRace, UserRaceId> {
    List<UserRace> findByUser_UID(String uid);
    Optional<UserRace> findByUserIdAndRaceId(Long userId, Long raceId);
    List<UserRace> findByUserIdAndStatus(Long userId, String status);
    Optional<UserRace> findByUser_UIDAndRace_Id(String uid, Long raceId);
}

