package com.running.endpoint.api;

import com.running.model.Career;
import com.running.model.CareerDto;
import com.running.service.OrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// OrganizerController.java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/organizer")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    // Lista de carreras del organizador (por uid Firebase)
    @GetMapping("/{uid}/races")
    public ResponseEntity<List<Career>> myRaces(@PathVariable String uid) {
        return ResponseEntity.ok(organizerService.listMyRaces(uid));
    }

    // Crear carrera como organizador (se asigna automáticamente)
    @PostMapping("/{uid}/races")
    public ResponseEntity<Career> create(@PathVariable String uid, @RequestBody CareerDto body) {
        return ResponseEntity.ok(organizerService.createAsOrganizer(uid, body));
    }

    // Editar carrera propia
    @PutMapping("/{uid}/races/{id}")
    public ResponseEntity<Career> update(@PathVariable String uid,
                                         @PathVariable Long id,
                                         @RequestBody CareerDto body) {
        return ResponseEntity.ok(organizerService.updateMyRace(uid, id, body));
    }

    // Borrar carrera propia
    @DeleteMapping("/{uid}/races/{id}")
    public ResponseEntity<Void> delete(@PathVariable String uid, @PathVariable Long id) {
        organizerService.deleteMyRace(uid, id);
        return ResponseEntity.noContent().build();
    }
}

