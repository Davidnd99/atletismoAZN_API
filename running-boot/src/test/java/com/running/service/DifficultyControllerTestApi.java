package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.DifficultyController;
import com.running.model.Difficulty;
import com.running.model.DifficultyDto;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DifficultyControllerTestApi {

    @Mock
    DifficultyService difficultyService;

    @InjectMocks
    DifficultyController controller;

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
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // -------- POST /api/difficulty/save --------

    @Test
    @DisplayName("POST /api/difficulty/save -> 200 y devuelve Difficulty creado")
    void create_ok() throws Exception {
        DifficultyDto dto = new DifficultyDto();
        dto.setName("Fácil");

        Difficulty created = Difficulty.builder().iddifficulty(1L).name("Fácil").build();
        when(difficultyService.save(any(DifficultyDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/difficulty/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.iddifficulty").value(1))
                .andExpect(jsonPath("$.name").value("Fácil"));

        verify(difficultyService).save(any(DifficultyDto.class));
    }

    @Test
    @DisplayName("POST /api/difficulty/save -> servicio lanza excepción -> 500")
    void create_error500() throws Exception {
        DifficultyDto dto = new DifficultyDto();
        dto.setName("Cualquier");

        when(difficultyService.save(any(DifficultyDto.class)))
                .thenThrow(new RuntimeException("boom save"));

        mockMvc.perform(post("/api/difficulty/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom save")));
    }

    // -------- GET /api/difficulty/getAll --------

    @Test
    @DisplayName("GET /api/difficulty/getAll -> 200 y lista con elementos")
    void getAll_ok() throws Exception {
        var d1 = Difficulty.builder().iddifficulty(10L).name("Fácil").build();
        var d2 = Difficulty.builder().iddifficulty(20L).name("Media").build();
        when(difficultyService.findAll()).thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/api/difficulty/getAll"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].iddifficulty").value(10))
                .andExpect(jsonPath("$[1].iddifficulty").value(20));

        verify(difficultyService).findAll();
    }

    @Test
    @DisplayName("GET /api/difficulty/getAll -> 200 y lista vacía")
    void getAll_empty() throws Exception {
        when(difficultyService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/difficulty/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/difficulty/getAll -> servicio lanza excepción -> 500")
    void getAll_error500() throws Exception {
        when(difficultyService.findAll()).thenThrow(new RuntimeException("boom list"));

        mockMvc.perform(get("/api/difficulty/getAll"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom list")));
    }
}
