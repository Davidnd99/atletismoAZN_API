package com.running.endpoint.api;

import com.running.model.*;
import com.running.service.CareerService;
import com.running.service.DifficultyService;
import com.running.service.ParticipantService;
import com.running.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final DifficultyService difficultyService;
    private final TypeService typeService;
    private final ParticipantService participantService;

    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Career> addCareer(@RequestBody CareerDto request) {
        return ResponseEntity.ok(careerService.save(request));
    }

    @GetMapping(value = "/getById", produces = "application/json")
    public ResponseEntity<Career> getById(@RequestParam Long id) {
        return ResponseEntity.ok(careerService.findById(id));
    }

    @GetMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<Career>> getAllCareers() {
        return ResponseEntity.ok(careerService.findAll());
    }

    @GetMapping(value = "/filter", produces = "application/json")
    public ResponseEntity<List<Career>> filterCareers(
            @RequestParam(required = false) String province,
            @RequestParam(required = false, name = "fechaDesde") String fechaDesdeStr,
            @RequestParam(required = false, name = "fechaHasta") String fechaHastaStr,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long difficultyId,
            @RequestParam(required = false) Boolean finalizada
    ) {
        LocalDateTime from = parseStart(fechaDesdeStr);
        LocalDateTime to   = parseEnd(fechaHastaStr);

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fechaDesde no puede ser posterior a fechaHasta"
            );
        }

        List<Career> result = careerService.filterCareers(
                province, from, to, typeId, difficultyId, finalizada
        );
        return ResponseEntity.ok(result);
    }

    // ---- Helpers de parseo tolerantes a formatos típicos ----
    private LocalDateTime parseStart(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim().replace(' ', 'T'); // soporta "YYYY-MM-DD HH:mm:ss"
        // Solo fecha -> inicio del día
        if (t.length() == 10) {
            LocalDate d = LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE);
            return d.atStartOfDay();
        }
        try {
            return LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato de fechaDesde inválido. Usa 'yyyy-MM-dd' o 'yyyy-MM-ddTHH:mm:ss'");
        }
    }

    private LocalDateTime parseEnd(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim().replace(' ', 'T');
        if (t.length() == 10) {
            LocalDate d = LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE);
            return d.atTime(23, 59, 59); // fin de día inclusivo
        }
        try {
            return LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato de fechaHasta inválido. Usa 'yyyy-MM-dd' o 'yyyy-MM-ddTHH:mm:ss'");
        }
    }

    @GetMapping(value = "/getByProvince", produces = "application/json")
    public ResponseEntity<List<Career>> getByProvince(@RequestParam String province) {
        return ResponseEntity.ok(careerService.findByProvince(province));
    }

    @GetMapping(value = "/getByType", produces = "application/json")
    public ResponseEntity<List<Career>> getByType(@RequestParam Long typeId) {
        Type type = typeService.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Type not found with id: " + typeId));
        return ResponseEntity.ok(careerService.findByType(type));
    }

    @GetMapping(value = "/getByDifficulty", produces = "application/json")
    public ResponseEntity<List<Career>> getByDifficulty(@RequestParam Long difficultyId) {
        Difficulty difficulty = difficultyService.findById(difficultyId)
                .orElseThrow(() -> new RuntimeException("Difficulty not found with id: " + difficultyId));
        return ResponseEntity.ok(careerService.findByDifficulty(difficulty));
    }

    @GetMapping(value = "/getByOrganizer", produces = "application/json")
    public ResponseEntity<List<Career>> getByOrganizer(@RequestParam Long organizerUserId) {
        return ResponseEntity.ok(careerService.findByOrganizer(organizerUserId));
    }

    /* ===== NUEVO: organizer de una carrera ===== */

    @GetMapping("/{id}/organizer")
    public ResponseEntity<OrganizerDto> getOrganizer(@PathVariable Long id) {
        return ResponseEntity.ok(careerService.getOrganizerOfRace(id));
    }

    @PutMapping("/{id}/organizer")
    public ResponseEntity<OrganizerDto> updateOrganizer(@PathVariable Long id,
                                                        @RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        return ResponseEntity.ok(careerService.updateRaceOrganizer(id, email));
    }

    /** Lista participantes (rol 'user') inscritos en una carrera.
     *  Permisos: admin o el organizador de la carrera.
     *  status (opcional): si se envía, filtra por estado (ej. 'confirmada', 'pendiente', etc).
     */
    @GetMapping("/{raceId}/participants")
    public ResponseEntity<List<ParticipantDto>> listParticipants(
            @PathVariable Long raceId,
            @RequestParam String uid,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(participantService.listParticipants(uid, raceId, status));
    }
}
