package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.TypeController;
import com.running.model.Type;
import com.running.model.TypeDto;
import com.running.service.TypeService;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TypeControllerTestApi {

    @Mock
    TypeService typeService;

    @InjectMocks
    TypeController controller;

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

    // Advice de prueba: RuntimeException -> 500
    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // -------- POST /api/type/save --------

    @Test
    @DisplayName("POST /api/type/save -> 200 y devuelve Type creado")
    void create_ok() throws Exception {
        TypeDto dto = new TypeDto();
        dto.setName("Trail");

        // usa builder porque lo usas en el servicio
        Type saved = Type.builder().name("Trail").build();
        when(typeService.save(any(TypeDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/type/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Trail"));

        verify(typeService).save(any(TypeDto.class));
    }

    @Test
    @DisplayName("POST /api/type/save -> servicio lanza excepción -> 500")
    void create_error500() throws Exception {
        TypeDto dto = new TypeDto();
        dto.setName("X");

        when(typeService.save(any(TypeDto.class))).thenThrow(new RuntimeException("boom save type"));

        mockMvc.perform(post("/api/type/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom save type")));
    }

    // -------- GET /api/type/getAll --------

    @Test
    @DisplayName("GET /api/type/getAll -> 200 y lista con elementos")
    void getAll_ok() throws Exception {
        var t1 = Type.builder().name("Asfalto").build();
        var t2 = Type.builder().name("Trail").build();
        when(typeService.findAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/type/getAll"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Asfalto"))
                .andExpect(jsonPath("$[1].name").value("Trail"));

        verify(typeService).findAll();
    }

    @Test
    @DisplayName("GET /api/type/getAll -> 200 y lista vacía")
    void getAll_empty() throws Exception {
        when(typeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/type/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/type/getAll -> servicio lanza excepción -> 500")
    void getAll_error500() throws Exception {
        when(typeService.findAll()).thenThrow(new RuntimeException("boom list type"));

        mockMvc.perform(get("/api/type/getAll"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom list type")));
    }
}
