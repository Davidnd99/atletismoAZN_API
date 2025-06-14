package com.running.endpoint.api;

import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.model.User;
import com.running.model.UserDto;
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
}
