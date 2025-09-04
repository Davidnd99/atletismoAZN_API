package com.running.service;

import com.running.model.*;
import com.running.model.ReassignmentLog.EntityType;
import com.running.model.ReassignedCareerDto;
import com.running.model.ReassignedClubDto;
import com.running.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReassignmentQueryService {

    private final ReassignmentLogRepository logRepo;
    private final CareerRepository careerRepo;
    private final ClubRepository clubRepo;

    @Transactional(readOnly = true)
    public List<ReassignedCareerDto> getReassignedCareersFor(String uid) {
        var logs = logRepo.findByToUser_UIDAndEntityTypeOrderByCreatedAtDesc(uid, EntityType.CAREER);

        // último log por carrera (si hubo varias reasignaciones)
        Map<Long, ReassignmentLog> latestByCareer = new LinkedHashMap<>();
        for (var log : logs) latestByCareer.putIfAbsent(log.getEntityId(), log);

        var ids = new ArrayList<>(latestByCareer.keySet());
        if (ids.isEmpty()) return List.of();

        var careers = careerRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Career::getId, c -> c));

        List<ReassignedCareerDto> out = new ArrayList<>();
        for (Long id : ids) {
            var c = careers.get(id);
            var l = latestByCareer.get(id);
            if (c != null && l != null) {
                String fromEmail = (l.getFromUser() != null)
                        ? l.getFromUser().getEmail()
                        : "usuario borrado"; // 👈 evita NPE si el usuario origen ya no existe

                out.add(ReassignedCareerDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .place(c.getPlace())
                        .distanceKm(c.getDistance_km())
                        .photo(c.getPhoto())
                        .date(c.getDate())
                        .reassignedFromEmail(fromEmail)
                        .reassignedAt(LocalDateTime.from(l.getCreatedAt()))
                        .build());
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<ReassignedClubDto> getReassignedClubsFor(String uid) {
        var logs = logRepo.findByToUser_UIDAndEntityTypeOrderByCreatedAtDesc(uid, EntityType.CLUB);

        Map<Long, ReassignmentLog> latestByClub = new LinkedHashMap<>();
        for (var log : logs) latestByClub.putIfAbsent(log.getEntityId(), log);

        var ids = new ArrayList<>(latestByClub.keySet());
        if (ids.isEmpty()) return List.of();

        var clubs = clubRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Club::getId, c -> c));

        List<ReassignedClubDto> out = new ArrayList<>();
        for (Long id : ids) {
            var c = clubs.get(id);
            var l = latestByClub.get(id);
            if (c != null && l != null) {
                String fromEmail = (l.getFromUser() != null)
                        ? l.getFromUser().getEmail()
                        : "usuario borrado"; // 👈 evita NPE

                out.add(ReassignedClubDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .province(c.getProvince())
                        .place(c.getPlace())
                        .members(c.getMembers())
                        .photo(c.getPhoto())
                        .reassignedFromEmail(fromEmail)
                        .reassignedAt(LocalDateTime.from(l.getCreatedAt()))
                        .build());
            }
        }
        return out;
    }
}
