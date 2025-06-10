// running-endpoint/src/main/java/com/running/endpoint/api/DifficultyController.java
package com.running.endpoint.api;

import com.running.model.Difficulty;
import com.running.model.DifficultyDto;
import com.running.service.DifficultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/difficulty")
@RequiredArgsConstructor
public class DifficultyController {

    private final DifficultyService difficultyService;

    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public Difficulty create(@RequestBody DifficultyDto dto) {
        return difficultyService.save(dto);
    }

    @GetMapping(value = "/getAll", produces = "application/json")
    public List<Difficulty> getAll() {
        return difficultyService.findAll();
    }
}