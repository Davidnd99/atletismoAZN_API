package com.running.repository;

import com.running.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByProvinceIgnoreCaseAndNameNotIgnoreCase(String province, String excludedName);

    List<Club> findByNameNotIgnoreCase(String excludedName);

    // NUEVOS (gestión)
    List<Club> findByManager_UIDOrderByNameAsc(String managerUid);   // por UID Firebase
    List<Club> findByManager_IdOrderByNameAsc(Long managerUserId);   // por id interno
    boolean existsByIdAndManager_Id(Long clubId, Long managerUserId);
}
