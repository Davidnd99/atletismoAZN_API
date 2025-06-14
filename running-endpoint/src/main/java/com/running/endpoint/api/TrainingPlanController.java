package com.running.endpoint.api;

import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/training-plans")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @PostMapping("/save")
    public ResponseEntity<TrainingPlan> create(@RequestBody TrainingPlanDto dto) {
        return ResponseEntity.ok(trainingPlanService.save(dto));
    }

    @GetMapping("/by-club")
    public ResponseEntity<List<TrainingPlan>> getByClub(@RequestParam Long idClub, @RequestParam String uid) {
        return ResponseEntity.ok(trainingPlanService.findByClubId(idClub, uid));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrainingPlan>> getAll() {
        return ResponseEntity.ok(trainingPlanService.findAll());
    }
}
