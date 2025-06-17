package com.running.repository;

import com.running.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findByProvinceIgnoreCaseAndNameNotIgnoreCase(String province, String excludedName);

    List<Club> findByNameNotIgnoreCase(String excludedName);
}
