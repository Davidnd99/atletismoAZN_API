package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.UserRaceController;
import com.running.model.MarcaDto;
import com.running.model.UserRaceResponseDto;
import com.running.service.UserRaceService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserRaceControllerTestApi {

    @Mock
    UserRaceService userRaceService;

    @InjectMocks
    UserRaceController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestAdvice())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @RestControllerAdvice
    static class TestAdvice {
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<String> handleRSE(ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- POST /pre-register/{raceId} ----------

    @Test
    @DisplayName("POST /api/user-race/pre-register/{raceId}?uid=u1 -> 200 y mensaje")
    void preRegister_ok() throws Exception {
        mockMvc.perform(post("/api/user-race/pre-register/{raceId}", 11)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pendiente")));

        verify(userRaceService).preRegister("u1", 11L);
    }

    @Test
    @DisplayName("POST /api/user-race/pre-register -> 500 si falla servicio")
    void preRegister_error500() throws Exception {
        doThrow(new RuntimeException("Carrera no encontrada"))
                .when(userRaceService).preRegister("u1", 99L);

        mockMvc.perform(post("/api/user-race/pre-register/{raceId}", 99)
                        .param("uid", "u1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Carrera no encontrada")));
    }

    // ---------- PUT /confirm/{raceId} ----------

    @Test
    @DisplayName("PUT /api/user-race/confirm/{raceId}?uid=u1 -> 200 y mensaje")
    void confirm_ok() throws Exception {
        mockMvc.perform(put("/api/user-race/confirm/{raceId}", 7)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("confirmada")));

        verify(userRaceService).confirmRegistration("u1", 7L);
    }

    @Test
    @DisplayName("PUT /api/user-race/confirm -> 500 si falla servicio")
    void confirm_error500() throws Exception {
        doThrow(new RuntimeException("Inscripcion no encontrada"))
                .when(userRaceService).confirmRegistration("u1", 7L);

        mockMvc.perform(put("/api/user-race/confirm/{raceId}", 7)
                        .param("uid", "u1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Inscripcion no encontrada")));
    }

    // ---------- PUT /cancel/{raceId} ----------

    @Test
    @DisplayName("PUT /api/user-race/cancel/{raceId}?uid=u1 -> 200 y mensaje")
    void cancel_ok() throws Exception {
        mockMvc.perform(put("/api/user-race/cancel/{raceId}", 5)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("cancelada")));

        verify(userRaceService).cancelRegistration("u1", 5L);
    }

    @Test
    @DisplayName("PUT /api/user-race/cancel -> 500 si falla servicio")
    void cancel_error500() throws Exception {
        doThrow(new RuntimeException("Usuario no encontrado"))
                .when(userRaceService).cancelRegistration("uX", 5L);

        mockMvc.perform(put("/api/user-race/cancel/{raceId}", 5)
                        .param("uid", "uX"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    // ---------- GET /list/{uid} ----------

    @Test
    @DisplayName("GET /api/user-race/list/{uid} -> 200 y lista")
    void getUserRaces_ok() throws Exception {
        var r1 = UserRaceResponseDto.builder().raceId(1L).status("pendiente").build();
        var r2 = UserRaceResponseDto.builder().raceId(2L).status("confirmada").build();

        when(userRaceService.getUserRaceDtos("u1")).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/user-race/list/{uid}", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].raceId").value(1))
                .andExpect(jsonPath("$[1].status").value("confirmada"));

        verify(userRaceService).getUserRaceDtos("u1");
    }

    // ---------- GET /status/{raceId}?uid=... ----------

    @Test
    @DisplayName("GET /api/user-race/status/{raceId}?uid=u1 -> 200 y status devuelto")
    void getInscriptionStatus_ok_value() throws Exception {
        when(userRaceService.getStatus("u1", 33L)).thenReturn("confirmada");

        mockMvc.perform(get("/api/user-race/status/{raceId}", 33)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmada"));

        verify(userRaceService).getStatus("u1", 33L);
    }

    @Test
    @DisplayName("GET /api/user-race/status/{raceId}?uid=u1 -> 200 y status=no_inscrito si null")
    void getInscriptionStatus_ok_noInscrito() throws Exception {
        when(userRaceService.getStatus("u1", 33L)).thenReturn(null);

        mockMvc.perform(get("/api/user-race/status/{raceId}", 33)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("no_inscrito"));
    }

    // ---------- GET /list-by-status/{uid}?status=... ----------

    @Test
    @DisplayName("GET /api/user-race/list-by-status/{uid}?status=pendiente -> 200 y lista")
    void getUserRacesByStatus_ok() throws Exception {
        when(userRaceService.getUserRacesByStatus("u1", "pendiente")).thenReturn(List.of());

        mockMvc.perform(get("/api/user-race/list-by-status/{uid}", "u1")
                        .param("status", "pendiente"))
                .andExpect(status().isOk());

        verify(userRaceService).getUserRacesByStatus("u1", "pendiente");
    }

    @Test
    @DisplayName("GET /api/user-race/list-by-status -> 500 si falla servicio")
    void getUserRacesByStatus_error500() throws Exception {
        when(userRaceService.getUserRacesByStatus("uX", "confirmada"))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/user-race/list-by-status/{uid}", "uX")
                        .param("status", "confirmada"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    // ---------- GET /{uid}/marcas ----------

    @Test
    @DisplayName("GET /api/user-race/{uid}/marcas -> 200 y lista")
    void getMarcas_ok() throws Exception {
        MarcaDto m1 = new MarcaDto(); m1.setRaceId(1L); m1.setTiempo("00:45:00");
        MarcaDto m2 = new MarcaDto(); m2.setRaceId(2L); m2.setTiempo(null);

        when(userRaceService.obtenerMarcas("u1")).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/user-race/{uid}/marcas", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].raceId").value(1));
    }

    // ---------- GET /{uid}/marcas/{raceId} ----------

    @Test
    @DisplayName("GET /api/user-race/{uid}/marcas/{raceId} -> 200 y devuelve marca")
    void getMarcaPorCarrera_ok() throws Exception {
        MarcaDto dto = new MarcaDto(); dto.setRaceId(9L); dto.setTiempo("01:10:00");
        when(userRaceService.obtenerMarcaPorCarrera("u1", 9L)).thenReturn(dto);

        mockMvc.perform(get("/api/user-race/{uid}/marcas/{raceId}", "u1", 9))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.raceId").value(9))
                .andExpect(jsonPath("$.tiempo").value("01:10:00"));

        verify(userRaceService).obtenerMarcaPorCarrera("u1", 9L);
    }

    @Test
    @DisplayName("GET /api/user-race/{uid}/marcas/{raceId} -> 500 si falla servicio")
    void getMarcaPorCarrera_error500() throws Exception {
        when(userRaceService.obtenerMarcaPorCarrera("u1", 9L))
                .thenThrow(new RuntimeException("No existe inscripcion para esa carrera"));

        mockMvc.perform(get("/api/user-race/{uid}/marcas/{raceId}", "u1", 9))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No existe inscripcion")));
    }

    // ---------- PUT /{uid}/marcas/{raceId} ----------

    @Test
    @DisplayName("PUT /api/user-race/{uid}/marcas/{raceId} -> 200 OK sin body")
    void actualizarMarca_ok() throws Exception {
        MarcaDto body = new MarcaDto();
        body.setTiempo("01:23:45");
        body.setPosicion(10);

        mockMvc.perform(put("/api/user-race/{uid}/marcas/{raceId}", "u1", 77)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userRaceService).actualizarMarca(eq("u1"), eq(77L), any(MarcaDto.class));
    }

    @Test
    @DisplayName("PUT /api/user-race/{uid}/marcas/{raceId} -> 500 si falla servicio")
    void actualizarMarca_error500() throws Exception {
        doThrow(new RuntimeException("Solo se pueden registrar marcas de carreras confirmadas"))
                .when(userRaceService).actualizarMarca(eq("u1"), eq(77L), any(MarcaDto.class));

        mockMvc.perform(put("/api/user-race/{uid}/marcas/{raceId}", "u1", 77)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tiempo\":\"01:02:03\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Solo se pueden registrar marcas")));
    }

    // ---------- DELETE /{uid}/marcas/{raceId} ----------

    @Test
    @DisplayName("DELETE /api/user-race/{uid}/marcas/{raceId} -> 200 OK sin body")
    void eliminarMarca_ok() throws Exception {
        mockMvc.perform(delete("/api/user-race/{uid}/marcas/{raceId}", "u1", 77))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userRaceService).eliminarMarca("u1", 77L);
    }

    @Test
    @DisplayName("DELETE /api/user-race/{uid}/marcas/{raceId} -> 500 si falla servicio")
    void eliminarMarca_error500() throws Exception {
        doThrow(new RuntimeException("Solo se pueden eliminar marcas de carreras confirmadas"))
                .when(userRaceService).eliminarMarca("u1", 77L);

        mockMvc.perform(delete("/api/user-race/{uid}/marcas/{raceId}", "u1", 77))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Solo se pueden eliminar marcas")));
    }
}
