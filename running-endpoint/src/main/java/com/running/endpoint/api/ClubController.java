package com.running.endpoint.api;

import com.running.model.AdminClubDto;
import com.running.model.ClubDto;
import com.running.model.UpdateManagerRequest;
import com.running.model.UserDto;
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

    @GetMapping("/all")
    public ResponseEntity<List<ClubDto>> getAllClubs(
            @RequestParam(required = false) String provincia) {
        return ResponseEntity.ok(clubService.getAllClubs(provincia));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ClubDto>> getClubsByUser(@RequestParam String uid) {
        return ResponseEntity.ok(clubService.getClubsByUser(uid));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubDto> getClubById(
            @PathVariable Long id,
            @RequestParam(required = false) String uid) {
        return ResponseEntity.ok(clubService.getClubById(id, uid));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<UserDto>> getClubMembers(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getUsersByClub(id));
    }

    @GetMapping("/{id}/AdminClub")
    public ResponseEntity<AdminClubDto> getClubManager(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getManagerOfClub(id));
    }

    // Actualizar datos del club (nombre, place, province, photo, contact)
    @PutMapping("/{id}")
    public ResponseEntity<ClubDto> updateClub(
            @PathVariable Long id,
            @RequestBody ClubDto body) {
        return ResponseEntity.ok(clubService.updateClub(id, body));
    }

    // Cambiar administrador del club por email
    @PutMapping("/{id}/manager")
    public ResponseEntity<AdminClubDto> updateClubManager(
            @PathVariable Long id,
            @RequestBody UpdateManagerRequest body) {
        return ResponseEntity.ok(clubService.updateClubManager(id, body.getEmail()));
    }
}
