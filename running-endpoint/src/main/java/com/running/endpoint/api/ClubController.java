package com.running.endpoint.api;

import com.running.model.ClubDto;
import com.running.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    // Endpoint 1: Obtener todos los clubes (con o sin filtro por provincia)
    @GetMapping("/all")
    public ResponseEntity<List<ClubDto>> getAllClubs(
            @RequestParam(required = false) String provincia) {
        return ResponseEntity.ok(clubService.getAllClubs(provincia));
    }

    // Endpoint 2: Obtener los clubes del usuario
    @GetMapping("/user")
    public ResponseEntity<List<ClubDto>> getClubsByUser(@RequestParam String uid) {
        return ResponseEntity.ok(clubService.getClubsByUser(uid));
    }

    // Endpoint 3: Obtener detalles de un club por ID (opcionalmente con uid)
    @GetMapping("/{id}")
    public ResponseEntity<ClubDto> getClubById(
            @PathVariable Long id,
            @RequestParam(required = false) String uid) {
        return ResponseEntity.ok(clubService.getClubById(id, uid));
    }
}
