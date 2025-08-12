package com.running.repository;

import com.running.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByProvinceIgnoreCaseAndNameNotIgnoreCase(String province, String excludedName);

    List<Club> findByNameNotIgnoreCase(String excludedName);

    // NUEVOS (gestión)
    List<Club> findByManager_UIDOrderByNameAsc(String managerUid);   // por UID Firebase
    @Query("select c from Club c left join fetch c.manager where c.id = :id")
    java.util.Optional<Club> findByIdWithManager(@Param("id") Long id);

}
