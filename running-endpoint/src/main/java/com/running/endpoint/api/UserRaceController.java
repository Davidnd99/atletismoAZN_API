package com.running.endpoint.api;

import com.running.model.MarcaDto;
import com.running.model.UserRace;
import com.running.model.UserRaceResponseDto;
import com.running.service.UserRaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user-race")
@RequiredArgsConstructor
public class UserRaceController {

    private final UserRaceService userRaceService;

    @PostMapping("/pre-register/{raceId}")
    public ResponseEntity<String> preRegister(@PathVariable Long raceId, @RequestParam String uid) {
        userRaceService.preRegister(uid, raceId);
        return ResponseEntity.ok("Inscripción pendiente registrada");
    }

    @PutMapping("/confirm/{raceId}")
    public ResponseEntity<String> confirm(@PathVariable Long raceId, @RequestParam String uid) {
        userRaceService.confirmRegistration(uid, raceId);
        return ResponseEntity.ok("Inscripción confirmada");
    }

    @PutMapping("/cancel/{raceId}")
    public ResponseEntity<String> cancel(@PathVariable Long raceId, @RequestParam String uid) {
        userRaceService.cancelRegistration(uid, raceId);
        return ResponseEntity.ok("Inscripción cancelada");
    }

    @GetMapping("/list/{uid}")
    public ResponseEntity<List<UserRaceResponseDto>> getUserRaces(@PathVariable String uid) {
        return ResponseEntity.ok(userRaceService.getUserRaceDtos(uid));
    }

    @GetMapping("/status/{raceId}")
    public ResponseEntity<Map<String, String>> getInscriptionStatus(@PathVariable Long raceId, @RequestParam String uid) {
        String status = userRaceService.getStatus(uid, raceId);
        Map<String, String> response = new HashMap<>();
        response.put("status", status != null ? status : "no_inscrito");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list-by-status/{uid}")
    public ResponseEntity<List<UserRaceResponseDto>> getUserRacesByStatus(
            @PathVariable String uid,
            @RequestParam String status) {
        return ResponseEntity.ok(userRaceService.getUserRacesByStatus(uid, status));
    }

    // 1) Listar todas las marcas del usuario
    @GetMapping("/{uid}/marcas")
    public ResponseEntity<List<MarcaDto>> getMarcas(@PathVariable String uid) {
        List<MarcaDto> marcas = userRaceService.obtenerMarcas(uid);
        return ResponseEntity.ok(marcas);
    }

    // 2) Obtener la marca de una carrera concreta
    @GetMapping("/{uid}/marcas/{raceId}")
    public ResponseEntity<MarcaDto> getMarcaPorCarrera(
            @PathVariable String uid,
            @PathVariable Long raceId) {
        MarcaDto marca = userRaceService.obtenerMarcaPorCarrera(uid, raceId);
        return ResponseEntity.ok(marca);
    }

    // 3) Registrar o actualizar una marca
    @PutMapping("/{uid}/marcas/{raceId}")
    public ResponseEntity<Void> actualizarMarca(
            @PathVariable String uid,
            @PathVariable Long raceId,
            @RequestBody MarcaDto dto) {
        userRaceService.actualizarMarca(uid, raceId, dto);
        return ResponseEntity.ok().build();
    }

    // 4) Eliminar una marca
    @DeleteMapping("/{uid}/marcas/{raceId}")
    public ResponseEntity<Void> eliminarMarca(
            @PathVariable String uid,
            @PathVariable Long raceId) {
        userRaceService.eliminarMarca(uid, raceId);
        return ResponseEntity.ok().build();
    }

}

