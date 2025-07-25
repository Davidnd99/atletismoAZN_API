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
    public ResponseEntity<AccessoriesDto> createAccessory(@RequestBody AccessoriesDto dto) throws Exception {
        return ResponseEntity.ok(service.save(dto));
    }

    @GetMapping(value = "/getAll", produces = "application/json")
    public ResponseEntity<List<Accessories>> getAllAccessories() {
        return ResponseEntity.ok(service.getAll());
    }
}

