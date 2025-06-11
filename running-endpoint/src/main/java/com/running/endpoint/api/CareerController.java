package com.running.endpoint.api;

import com.running.model.Career;
import com.running.model.CareerDto;
import com.running.model.Difficulty;
import com.running.model.Type;
import com.running.service.CareerService;
import com.running.service.DifficultyService;
import com.running.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final DifficultyService difficultyService;
    private final TypeService typeService;

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
}
