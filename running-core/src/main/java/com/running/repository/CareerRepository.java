package com.running.repository;

import com.running.model.Career;
import com.running.model.Type;
import com.running.model.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findByProvince(String province);
    List<Career> findByType(Type type);
    List<Career> findByDifficulty(Difficulty difficulty);
    Optional<Career> findById(Long id);
    List<Career> findByOrganizer_IdOrderByDateDesc(Long organizerUserId);
    List<Career> findByOrganizer_UIDOrderByDateDesc(String organizerUid);
    boolean existsByIdAndOrganizer_Id(Long careerId, Long organizerUserId);

    // NUEVO: cargar carrera con organizador (join fetch)
    @Query("select c from Career c left join fetch c.organizer where c.id = :id")
    Optional<Career> findByIdWithOrganizer(@Param("id") Long id);

    @Query("SELECT c FROM Career c " +
            "WHERE (:province IS NULL OR c.province = :province) " +
            "AND (:typeId IS NULL OR c.type.id_type = :typeId) " +
            "AND (:difficultyId IS NULL OR c.difficulty.iddifficulty = :difficultyId) " +
            "AND (:fechaDesde IS NULL OR c.date >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR c.date <= :fechaHasta) " +
            "AND (:finalizada IS NULL OR " +
            "     (:finalizada = TRUE  AND c.date < :now) OR " +
            "     (:finalizada = FALSE AND c.date >= :now)) " +
            "ORDER BY c.date ASC")
    List<Career> filterCareers(
            @Param("province") String province,
            @Param("typeId") Long typeId,
            @Param("difficultyId") Long difficultyId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("finalizada") Boolean finalizada,
            @Param("now") LocalDateTime now
    );
}
