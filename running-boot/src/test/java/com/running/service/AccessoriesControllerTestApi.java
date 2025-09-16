package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.AccessoriesController;
import com.running.model.Accessories;
import com.running.model.AccessoriesDto;
import com.running.service.AccessoriesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AccessoriesControllerTestApi {

    @Mock
    AccessoriesService service;

    @InjectMocks
    AccessoriesController controller;

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
        assertNotNull(controller);
    }

    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<String> handleRSE(ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @Test
    @DisplayName("POST /api/accessories/save -> 200 OK y devuelve el DTO creado")
    void createAccessory_ok() throws Exception {
        AccessoriesDto req = new AccessoriesDto();
        AccessoriesDto resp = new AccessoriesDto();
        when(service.save(any(AccessoriesDto.class))).thenReturn(resp);

        mockMvc.perform(post("/api/accessories/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));

        ArgumentCaptor<AccessoriesDto> captor = ArgumentCaptor.forClass(AccessoriesDto.class);
        verify(service).save(captor.capture());
    }

    @Test
    @DisplayName("GET /api/accessories/getAll -> 200 OK y lista de 2")
    void getAllAccessories_ok() throws Exception {
        when(service.getAll()).thenReturn(List.of(new Accessories(), new Accessories()));

        mockMvc.perform(get("/api/accessories/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).getAll();
    }

    @Test
    @DisplayName("GET /api/accessories/getById?id=7 -> 200 OK con body")
    void getById_ok() throws Exception {
        Accessories acc = new Accessories();
        when(service.getById(7L)).thenReturn(acc);

        mockMvc.perform(get("/api/accessories/getById").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(acc)));

        verify(service).getById(7L);
    }

    @Test
    @DisplayName("PUT /api/accessories/update?id=5 -> 200 OK y devuelve DTO")
    void update_ok() throws Exception {
        AccessoriesDto req = new AccessoriesDto();
        AccessoriesDto resp = new AccessoriesDto();
        when(service.update(eq(5L), any(AccessoriesDto.class))).thenReturn(resp);

        mockMvc.perform(put("/api/accessories/update")
                        .param("id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));

        verify(service).update(eq(5L), any(AccessoriesDto.class));
    }

    @Test
    @DisplayName("DELETE /api/accessories/delete?id=9 -> 204 No Content")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/accessories/delete").param("id", "9"))
                .andExpect(status().isNoContent());

        verify(service).delete(9L);
    }

    @Test
    @DisplayName("GET /api/accessories/getById -> si el servicio lanza RuntimeException -> 500")
    void getById_serviceThrows_500() throws Exception {
        when(service.getById(123L)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/accessories/getById").param("id", "123"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom")));
    }

    @Test
    @DisplayName("GET /api/accessories/getById -> si el servicio lanza NOT_FOUND -> 404")
    void getById_serviceThrows_404() throws Exception {
        when(service.getById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Accessory not found"));

        mockMvc.perform(get("/api/accessories/getById").param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Accessory not found")));
    }
}
