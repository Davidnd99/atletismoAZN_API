package com.running.endpoint.api;

import com.running.model.Career;
import com.running.model.CareerDto;
import com.running.service.CareerService;
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

    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Career> addCareer(@RequestBody CareerDto request) {
        return ResponseEntity.ok(careerService.save(request));
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
    public ResponseEntity<List<Career>> getByProvince(@RequestParam int type) {
        return ResponseEntity.ok(careerService.findByType(type));
    }
}
