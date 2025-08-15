package com.running.endpoint.api;

import com.running.model.Accessories;
import com.running.model.AccessoriesDto;
import com.running.service.AccessoriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/accessories")
@RequiredArgsConstructor
public class AccessoriesController {

    private final AccessoriesService service;

    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AccessoriesDto> createAccessory(@RequestBody AccessoriesDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @GetMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<Accessories>> getAllAccessories() {
        return ResponseEntity.ok(service.getAll());
    }

    // === NUEVOS ===

    @GetMapping(value = "/getById", produces = "application/json")
    public ResponseEntity<Accessories> getById(@RequestParam Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping(value = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AccessoriesDto> update(@RequestParam Long id, @RequestBody AccessoriesDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
