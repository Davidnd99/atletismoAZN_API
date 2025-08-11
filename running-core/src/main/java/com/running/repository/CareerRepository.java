package com.running.repository;

import com.running.model.Career;
import com.running.model.Type;
import com.running.model.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
