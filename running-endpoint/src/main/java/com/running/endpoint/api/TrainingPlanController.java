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

    // CREATE (ahora requiere uid para control de permisos)
    @PostMapping("/save")
    public ResponseEntity<TrainingPlan> create(@RequestParam String uid, @RequestBody TrainingPlanDto dto) {
        return ResponseEntity.ok(trainingPlanService.save(uid, dto));
    }

    // READ by club
    @GetMapping("/by-club")
    public ResponseEntity<List<TrainingPlan>> getByClub(@RequestParam Long idClub) {
        return ResponseEntity.ok(trainingPlanService.findByClubId(idClub));
    }

    // READ all
    @GetMapping("/all")
    public ResponseEntity<List<TrainingPlan>> getAll() {
        return ResponseEntity.ok(trainingPlanService.findAll());
    }

    // ====== NUEVOS ======

    // READ by id
    @GetMapping("/getById")
    public ResponseEntity<TrainingPlan> getById(@RequestParam Long id) {
        return ResponseEntity.ok(trainingPlanService.getById(id));
    }

    // UPDATE
    @PutMapping("/update")
    public ResponseEntity<TrainingPlan> update(@RequestParam Long id,
                                               @RequestParam String uid,
                                               @RequestBody TrainingPlanDto dto) {
        return ResponseEntity.ok(trainingPlanService.update(uid, id, dto));
    }

    // DELETE
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id, @RequestParam String uid) {
        trainingPlanService.delete(uid, id);
        return ResponseEntity.noContent().build();
    }
}
