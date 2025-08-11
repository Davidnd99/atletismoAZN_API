package com.running.endpoint.api;

import com.running.model.UserRaceResponseDto;
import com.running.service.AdminUserRaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/registrations")
@RequiredArgsConstructor
public class AdminUserRaceController {

    private final AdminUserRaceService service;

    // Inscripciones PENDIENTE de todas las carreras de un organizador
    @GetMapping("/pending/by-organizer/{uid}")
    public ResponseEntity<List<UserRaceResponseDto>> pendingByOrganizer(@PathVariable String uid) {
        return ResponseEntity.ok(service.listPendingByOrganizer(uid));
    }

    // Inscripciones PENDIENTE de una carrera concreta
    @GetMapping("/pending/by-race/{raceId}")
    public ResponseEntity<List<UserRaceResponseDto>> pendingByRace(@PathVariable Long raceId) {
        return ResponseEntity.ok(service.listPendingByRace(raceId));
    }

    // Cancelar TODAS las PENDIENTE de un organizador
    @PutMapping("/cancel/by-organizer/{uid}")
    public ResponseEntity<Integer> cancelByOrganizer(@PathVariable String uid) {
        int count = service.cancelAllPendingByOrganizer(uid);
        return ResponseEntity.ok(count);
    }

    // Cancelar TODAS las PENDIENTE de una carrera
    @PutMapping("/cancel/by-race/{raceId}")
    public ResponseEntity<Integer> cancelByRace(@PathVariable Long raceId) {
        int count = service.cancelAllPendingByRace(raceId);
        return ResponseEntity.ok(count);
    }
}


