package com.running.repository;

import com.running.model.Club;
import com.running.model.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    List<TrainingPlan> findByClub(Club club);

    boolean existsByClub_IdAndNameIgnoreCase(Long clubId, String name);
    boolean existsByClub_IdAndNameIgnoreCaseAndIdNot(Long clubId, String name, Long id);
}
