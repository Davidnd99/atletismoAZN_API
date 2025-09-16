package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.ClubAdminController;
import com.running.model.ClubDto;
import com.running.service.ClubAdminService;
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
public class ClubAdminControllerTestApi {

    @Mock
    ClubAdminService service;

    @InjectMocks
    ClubAdminController controller;

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

    // -------- GET /{uid}/clubs --------

    @Test
    @DisplayName("GET /api/club-admin/{uid}/clubs -> 200 y lista con elementos")
    void myClubs_ok() throws Exception {
        var c1 = ClubDto.builder().id(1L).name("Club A").province("Sevilla").place("Sevilla").members(10).photo("a.jpg").contact("a@a.com").joined(false).build();
        var c2 = ClubDto.builder().id(2L).name("Club B").province("Cádiz").place("Cádiz").members(20).photo("b.jpg").contact("b@b.com").joined(false).build();

        when(service.listMyClubs("u1")).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/club-admin/{uid}/clubs", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(service).listMyClubs("u1");
    }

    @Test
    @DisplayName("GET /api/club-admin/{uid}/clubs -> 200 y lista vacía")
    void myClubs_empty() throws Exception {
        when(service.listMyClubs("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/club-admin/{uid}/clubs", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/club-admin/{uid}/clubs -> servicio lanza excepción -> 500")
    void myClubs_error500() throws Exception {
        when(service.listMyClubs("boom")).thenThrow(new RuntimeException("boom clubs"));

        mockMvc.perform(get("/api/club-admin/{uid}/clubs", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom clubs")));
    }

    // -------- POST /{uid}/clubs --------

    @Test
    @DisplayName("POST /api/club-admin/{uid}/clubs -> 200 crea y devuelve club")
    void create_ok() throws Exception {
        var req = ClubDto.builder().name("Nuevo Club").province("Sevilla").place("Sevilla").members(15).photo("n.jpg").contact("n@n.com").joined(false).build();
        var created = ClubDto.builder().id(10L).name("Nuevo Club").province("Sevilla").place("Sevilla").members(15).photo("n.jpg").contact("n@n.com").joined(false).build();

        when(service.createAsManager(eq("u-admin"), any(ClubDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/club-admin/{uid}/clubs", "u-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Nuevo Club"));

        verify(service).createAsManager(eq("u-admin"), any(ClubDto.class));
    }

    @Test
    @DisplayName("POST /api/club-admin/{uid}/clubs -> servicio lanza excepción -> 500")
    void create_error500() throws Exception {
        var req = ClubDto.builder().name("X").build();
        when(service.createAsManager(eq("bad"), any(ClubDto.class))).thenThrow(new RuntimeException("not allowed"));

        mockMvc.perform(post("/api/club-admin/{uid}/clubs", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("not allowed")));
    }

    // -------- PUT /{uid}/clubs/{id} --------

    @Test
    @DisplayName("PUT /api/club-admin/{uid}/clubs/{id} -> 200 y devuelve club actualizado")
    void update_ok() throws Exception {
        var req = ClubDto.builder().name("Club Editado").province("Huelva").build();
        var updated = ClubDto.builder().id(5L).name("Club Editado").province("Huelva").place("Huelva").members(12).photo("e.jpg").contact("e@e.com").joined(false).build();

        when(service.updateMyClub(eq("u-admin"), eq(5L), any(ClubDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/club-admin/{uid}/clubs/{id}", "u-admin", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Club Editado"));

        verify(service).updateMyClub(eq("u-admin"), eq(5L), any(ClubDto.class));
    }

    @Test
    @DisplayName("PUT /api/club-admin/{uid}/clubs/{id} -> servicio lanza excepción -> 500")
    void update_error500() throws Exception {
        var req = ClubDto.builder().name("X").build();
        when(service.updateMyClub(eq("u-2"), eq(9L), any(ClubDto.class)))
                .thenThrow(new RuntimeException("No puedes gestionar este club"));

        mockMvc.perform(put("/api/club-admin/{uid}/clubs/{id}", "u-2", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No puedes gestionar este club")));
    }

    // -------- DELETE /{uid}/clubs/{id} --------

    @Test
    @DisplayName("DELETE /api/club-admin/{uid}/clubs/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/club-admin/{uid}/clubs/{id}", "u-admin", 7))
                .andExpect(status().isNoContent());

        verify(service).deleteMyClub("u-admin", 7L);
    }

    @Test
    @DisplayName("DELETE /api/club-admin/{uid}/clubs/{id} -> servicio lanza excepción -> 500")
    void delete_error500() throws Exception {
        doThrow(new RuntimeException("No puedes borrar este club"))
                .when(service).deleteMyClub("u-3", 8L);

        mockMvc.perform(delete("/api/club-admin/{uid}/clubs/{id}", "u-3", 8))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No puedes borrar este club")));
    }
}
