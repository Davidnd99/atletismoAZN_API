package com.running.endpoint.api;

import com.running.model.ClubDto;
import com.running.service.ClubAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/club-admin")
@RequiredArgsConstructor
public class ClubAdminController {

    private final ClubAdminService service;

    // Mis clubs (por UID Firebase)
    @GetMapping("/{uid}/clubs")
    public ResponseEntity<List<ClubDto>> myClubs(@PathVariable String uid) {
        return ResponseEntity.ok(service.listMyClubs(uid));
    }

    // Crear un club
    @PostMapping("/{uid}/clubs")
    public ResponseEntity<ClubDto> create(@PathVariable String uid, @RequestBody ClubDto body) {
        return ResponseEntity.ok(service.createAsManager(uid, body));
    }

    // Actualizar un club propio
    @PutMapping("/{uid}/clubs/{id}")
    public ResponseEntity<ClubDto> update(@PathVariable String uid,
                                          @PathVariable Long id,
                                          @RequestBody ClubDto body) {
        return ResponseEntity.ok(service.updateMyClub(uid, id, body));
    }

    // Borrar un club propio
    @DeleteMapping("/{uid}/clubs/{id}")
    public ResponseEntity<Void> delete(@PathVariable String uid, @PathVariable Long id) {
        service.deleteMyClub(uid, id);
        return ResponseEntity.noContent().build();
    }
}

