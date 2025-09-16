package com.running.endpoint.api;

import com.running.model.Type;
import com.running.model.TypeDto;
import com.running.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/type")
@RequiredArgsConstructor
public class TypeController {

    private final TypeService typeService;

    @PostMapping(value = "/save", consumes = "application/json", produces = "application/json")
    public Type create(@RequestBody TypeDto dto) {
        return typeService.save(dto);
    }

    @GetMapping(value = "/getAll", produces = "application/json")
    public List<Type> getAll() {
        return typeService.findAll();
    }
}