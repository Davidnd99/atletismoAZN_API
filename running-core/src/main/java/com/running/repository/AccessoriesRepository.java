package com.running.repository;

import com.running.model.Accessories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessoriesRepository extends JpaRepository<Accessories, Long> {
}
