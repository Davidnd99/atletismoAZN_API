package com.running.endpoint.api;

import com.running.model.*;
import com.running.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ Endpoint para unirse a un club
    @PutMapping("/{uid}/join-club/{clubId}")
    public ResponseEntity<String> joinClub(@PathVariable String uid, @PathVariable Long clubId) {
        userService.joinClub(uid, clubId);
        return ResponseEntity.ok("User joined the club successfully");
    }

    // ✅ Endpoint para salir de un club
    @PutMapping("/{uid}/leave-club/{clubId}")
    public ResponseEntity<String> leaveClub(@PathVariable String uid, @PathVariable Long clubId) {
        userService.leaveClub(uid, clubId);
        return ResponseEntity.ok("User left the club successfully");
    }

    @PostMapping("/save")
    public ResponseEntity<User> save(@RequestBody UserDto userDto) {
        User savedUser = userService.saveFromDto(userDto);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/findByUID/{uid}")
    public ResponseEntity<User> findByUID(@PathVariable String uid) {
        return ResponseEntity.ok(userService.findByUID(uid));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @DeleteMapping("/delete/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable String uid) {
        userService.deleteByUID(uid);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/role/{uid}")
    public ResponseEntity<RoleDto> getUserRole(@PathVariable String uid) {
        RoleDto roleDto = userService.getUserRoleByUID(uid);
        return ResponseEntity.ok(roleDto);
    }

    @PutMapping("/update/{uid}")
    public ResponseEntity<User> updateNameAndSurname(
            @PathVariable String uid,
            @RequestBody UserDto dto) {
        User updatedUser = userService.updateNameAndSurname(uid, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/roles/names")
    public ResponseEntity<List<String>> getAllRoleNames() {
        List<String> roleNames = userService.getAllRoleNames();
        return ResponseEntity.ok(roleNames);
    }

    @PostMapping("/save-admin")
    public ResponseEntity<User> saveUserFromAdmin(@RequestBody UserDto dto) {
        User user = userService.saveFromAdminDto(dto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/admin-create")
    public ResponseEntity<User> createUserWithFirebase(@RequestBody UserDto dto) {
        User user = userService.createUserWithFirebase(dto);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/admin-delete/{uid}")
    public ResponseEntity<String> adminDelete(
            @PathVariable String uid,
            @RequestParam("actingUid") String actingUid) {
        userService.adminDeleteWithFirebase(uid, actingUid);
        return ResponseEntity.ok("User reassigned (if organizer) and deleted from DB + Firebase");
    }


    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmailDto(email));
    }
}
