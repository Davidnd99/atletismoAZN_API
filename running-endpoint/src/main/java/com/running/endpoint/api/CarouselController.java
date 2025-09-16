package com.running.endpoint.api;

import com.running.model.CarouselItem;
import com.running.model.CarouselItemDto;
import com.running.service.CarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;

    // Listar todas las imágenes del carrusel
    @GetMapping("/getAll")
    public ResponseEntity<List<CarouselItem>> getAll() {
        return ResponseEntity.ok(carouselService.findAll());
    }

    // Obtener una imagen por id
    @GetMapping("/getById")
    public ResponseEntity<CarouselItem> getById(@RequestParam Long id) {
        return ResponseEntity.ok(carouselService.findById(id));
    }

    // Crear una imagen (solo admin)
    @PostMapping("/save")
    public ResponseEntity<CarouselItem> create(@RequestParam String uid,
                                               @RequestBody CarouselItemDto dto) {
        return ResponseEntity.ok(carouselService.create(uid, dto));
    }

    // Actualizar una imagen (solo admin)
    @PutMapping("/update")
    public ResponseEntity<CarouselItem> update(@RequestParam Long id,
                                               @RequestParam String uid,
                                               @RequestBody CarouselItemDto dto) {
        return ResponseEntity.ok(carouselService.update(uid, id, dto));
    }

    // Eliminar una imagen (solo admin)
    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id,
                                       @RequestParam String uid) {
        carouselService.delete(uid, id);
        return ResponseEntity.noContent().build();
    }
}
