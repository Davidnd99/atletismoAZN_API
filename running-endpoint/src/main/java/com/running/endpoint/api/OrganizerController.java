package com.running.endpoint.api;

import com.running.model.Race;
import com.running.model.RaceDto;
import com.running.service.OrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Misma ruta /api/organizer/{uid}/races...
 * Si {uid} tiene rol ADMIN, puede operar sobre cualquier carrera.
 * Si {uid} tiene rol ORGANIZATOR, solo sobre sus carreras.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/organizer")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService service;

    @GetMapping("/{uid}/races")
    public ResponseEntity<List<Race>> myRaces(@PathVariable String uid) {
        return ResponseEntity.ok(service.listMyRaces(uid));
    }

    @PostMapping("/{uid}/races")
    public ResponseEntity<Race> create(@PathVariable String uid, @RequestBody RaceDto body) {
        return ResponseEntity.ok(service.createAsOrganizer(uid, body));
    }

    @PutMapping("/{uid}/races/{id}")
    public ResponseEntity<Race> update(@PathVariable String uid,
                                       @PathVariable Long id,
                                       @RequestBody RaceDto body) {
        return ResponseEntity.ok(service.updateMyRace(uid, id, body));
    }

    @DeleteMapping("/{uid}/races/{id}")
    public ResponseEntity<Void> delete(@PathVariable String uid, @PathVariable Long id) {
        service.deleteMyRace(uid, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uid}/races/{raceId}/registrations/cancel-pending")
    public ResponseEntity<String> cancelPendingForUser(@PathVariable String uid,
                                                       @PathVariable Long raceId,
                                                       @RequestParam String userUid) {
        service.cancelPendingRegistration(uid, raceId, userUid);
        return ResponseEntity.ok("Inscripción pendiente cancelada");
    }

}
