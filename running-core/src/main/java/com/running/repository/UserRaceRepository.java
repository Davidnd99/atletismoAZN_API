package com.running.repository;

import com.running.model.ParticipantDto;
import com.running.model.UserRace;
import com.running.model.UserRaceId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRaceRepository extends JpaRepository<UserRace, UserRaceId> {

    List<UserRace> findByUser_UID(String uid);
    Optional<UserRace> findByUserIdAndRaceId(Long userId, Long raceId);
    List<UserRace> findByUserIdAndStatus(Long userId, String status);
    Optional<UserRace> findByUser_UIDAndRace_Id(String uid, Long raceId);

    List<UserRace> findByRace_Organizer_UIDAndStatus(String organizerUid, String status);

    List<UserRace> findByRace_Organizer_IdAndStatus(Long organizerId, String status);

    // Pendientes de una carrera concreta
    List<UserRace> findByRace_IdAndStatus(Long raceId, String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update UserRace ur
              set ur.status = 'cancelada'
            where ur.race.organizer.id = :organizerId
              and ur.status = 'pendiente'
           """)
    int cancelAllPendingByOrganizerId(@Param("organizerId") Long organizerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update UserRace ur
              set ur.status = 'cancelada'
            where ur.race.id = :raceId
              and ur.status = 'pendiente'
           """)
    int cancelAllPendingByRace(@Param("raceId") Long raceId);

    @Query("""
           SELECT new com.running.model.ParticipantDto(u.UID, u.name, u.email)
           FROM UserRace ur
           JOIN ur.user u
           JOIN u.role r
           WHERE ur.race.id = :raceId
             AND LOWER(r.name) = 'user'
             AND (:status IS NULL OR LOWER(ur.status) = LOWER(:status))
           """)
    List<ParticipantDto> findParticipantsByRaceAndRoleUser(
            @Param("raceId") Long raceId,
            @Param("status") String status
    );

    @Query("""
           SELECT ur FROM UserRace ur
           JOIN ur.user u
           WHERE ur.race.id = :raceId
             AND u.UID = :userUid
             AND LOWER(ur.status) = 'pendiente'
           """)
    Optional<UserRace> findPendingByRaceIdAndUserUid(@Param("raceId") Long raceId,
                                                     @Param("userUid") String userUid);
}

