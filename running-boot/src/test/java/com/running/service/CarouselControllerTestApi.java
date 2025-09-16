package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.CarouselController;
import com.running.model.CarouselItem;
import com.running.model.CarouselItemDto;
import com.running.service.CarouselService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CarouselControllerTestApi {

    @Mock
    CarouselService carouselService;

    @InjectMocks
    CarouselController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestGlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<String> handleRSE(ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- GET /getAll ----------

    @Test
    @DisplayName("GET /api/carousel/getAll -> 200 y lista con elementos")
    void getAll_ok() throws Exception {
        var i1 = CarouselItem.builder().id(1L).photo("a.jpg").build();
        var i2 = CarouselItem.builder().id(2L).photo("b.jpg").build();
        when(carouselService.findAll()).thenReturn(List.of(i1, i2));

        mockMvc.perform(get("/api/carousel/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(carouselService).findAll();
    }

    @Test
    @DisplayName("GET /api/carousel/getAll -> 200 y lista vacía")
    void getAll_empty() throws Exception {
        when(carouselService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/carousel/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /getById ----------

    @Test
    @DisplayName("GET /api/carousel/getById?id=7 -> 200 y devuelve item")
    void getById_ok() throws Exception {
        var item = CarouselItem.builder().id(7L).photo("x.jpg").build();
        when(carouselService.findById(7L)).thenReturn(item);

        mockMvc.perform(get("/api/carousel/getById").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.photo").value("x.jpg"));

        verify(carouselService).findById(7L);
    }

    @Test
    @DisplayName("GET /api/carousel/getById?id=99 -> 404 si no existe")
    void getById_notFound() throws Exception {
        when(carouselService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Carousel image not found"));

        mockMvc.perform(get("/api/carousel/getById").param("id", "99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Carousel image not found")));
    }

    // ---------- POST /save ----------

    @Test
    @DisplayName("POST /api/carousel/save -> 200 crea y devuelve item")
    void create_ok() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("new.jpg");

        var saved = CarouselItem.builder().id(10L).photo("new.jpg").build();
        when(carouselService.create(eq("admin-1"), any(CarouselItemDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/carousel/save")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.photo").value("new.jpg"));

        verify(carouselService).create(eq("admin-1"), any(CarouselItemDto.class));
    }

    @Test
    @DisplayName("POST /api/carousel/save -> 403 si no es admin")
    void create_forbidden() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("x.jpg");

        when(carouselService.create(eq("user-1"), any(CarouselItemDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can manage carousel"));

        mockMvc.perform(post("/api/carousel/save")
                        .param("uid", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin")));
    }

    @Test
    @DisplayName("POST /api/carousel/save -> 400 si photo inválida")
    void create_badRequest() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("   ");

        when(carouselService.create(eq("admin-1"), any(CarouselItemDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "photo is required"));

        mockMvc.perform(post("/api/carousel/save")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("photo is required")));
    }

    // ---------- PUT /update ----------

    @Test
    @DisplayName("PUT /api/carousel/update -> 200 y devuelve item actualizado")
    void update_ok() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("upd.jpg");

        var updated = CarouselItem.builder().id(5L).photo("upd.jpg").build();
        when(carouselService.update(eq("admin-1"), eq(5L), any(CarouselItemDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/carousel/update")
                        .param("id", "5")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.photo").value("upd.jpg"));

        verify(carouselService).update(eq("admin-1"), eq(5L), any(CarouselItemDto.class));
    }

    @Test
    @DisplayName("PUT /api/carousel/update -> 404 si no existe el item")
    void update_notFound() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("upd.jpg");

        when(carouselService.update(eq("admin-1"), eq(999L), any(CarouselItemDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Carousel image not found"));

        mockMvc.perform(put("/api/carousel/update")
                        .param("id", "999")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Carousel image not found")));
    }

    @Test
    @DisplayName("PUT /api/carousel/update -> 403 si no es admin")
    void update_forbidden() throws Exception {
        var dto = new CarouselItemDto();
        dto.setPhoto("upd.jpg");

        when(carouselService.update(eq("user-1"), eq(5L), any(CarouselItemDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can manage carousel"));

        mockMvc.perform(put("/api/carousel/update")
                        .param("id", "5")
                        .param("uid", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin")));
    }

    // ---------- DELETE /delete ----------

    @Test
    @DisplayName("DELETE /api/carousel/delete -> 204 No Content")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/carousel/delete")
                        .param("id", "9")
                        .param("uid", "admin-1"))
                .andExpect(status().isNoContent());

        verify(carouselService).delete("admin-1", 9L);
    }

    @Test
    @DisplayName("DELETE /api/carousel/delete -> 403 si no es admin")
    void delete_forbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can manage carousel"))
                .when(carouselService).delete("user-1", 9L);

        mockMvc.perform(delete("/api/carousel/delete")
                        .param("id", "9")
                        .param("uid", "user-1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Only admin")));
    }
}
