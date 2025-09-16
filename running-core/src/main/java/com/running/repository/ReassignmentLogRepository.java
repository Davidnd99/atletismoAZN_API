package com.running.repository;

import com.running.model.ReassignmentLog;
import com.running.model.ReassignmentLog.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReassignmentLogRepository extends JpaRepository<ReassignmentLog, Long> {
    List<ReassignmentLog> findByToUser_UIDAndEntityTypeOrderByCreatedAtDesc(
            String toUserUid, EntityType entityType);
}
