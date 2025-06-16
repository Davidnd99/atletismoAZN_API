package com.running.repository;

import com.running.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    List<Club> findByProvinceIgnoreCase(String province);
}
