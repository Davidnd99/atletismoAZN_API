package com.running.repository;

import com.running.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {

    List<Race> findByProvince(String province);
    List<Race> findByType(Type type);
    List<Race> findByDifficulty(Difficulty difficulty);
    Optional<Race> findById(Long id);
    List<Race> findByOrganizer_IdOrderByDateDesc(Long organizerUserId);
    List<Race> findByOrganizer_UIDOrderByDateDesc(String organizerUid);
    boolean existsByIdAndOrganizer_Id(Long raceId, Long organizerUserId);

    Optional<Race> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select c from Race c left join fetch c.organizer where c.id = :id")
    Optional<Race> findByIdWithOrganizer(@Param("id") Long id);

    @Query("SELECT c FROM Race c " +
            "WHERE (:province IS NULL OR c.province = :province) " +
            "AND (:typeId IS NULL OR c.type.id_type = :typeId) " +
            "AND (:difficultyId IS NULL OR c.difficulty.iddifficulty = :difficultyId) " +
            "AND (:fechaDesde IS NULL OR c.date >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR c.date <= :fechaHasta) " +
            "AND (:finalizada IS NULL OR " +
            "     (:finalizada = TRUE  AND c.date < :now) OR " +
            "     (:finalizada = FALSE AND c.date >= :now)) " +
            "ORDER BY c.date ASC")
    List<Race> filterRaces(
            @Param("province") String province,
            @Param("typeId") Long typeId,
            @Param("difficultyId") Long difficultyId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("finalizada") Boolean finalizada,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Race c SET c.organizer = :newOrganizer WHERE c.organizer = :oldOrganizer")
    int reassignOrganizer(@Param("oldOrganizer") User oldOrganizer,
                          @Param("newOrganizer") User newOrganizer);

    @Query("select c.id from Race c where c.organizer = :organizer")
    List<Long> findIdsByOrganizer(@Param("organizer") User organizer);
}
