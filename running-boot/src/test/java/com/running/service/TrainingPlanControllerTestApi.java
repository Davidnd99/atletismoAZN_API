package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.TrainingPlanController;
import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.service.TrainingPlanService;
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
public class TrainingPlanControllerTestApi {

    @Mock
    TrainingPlanService trainingPlanService;

    @InjectMocks
    TrainingPlanController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
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

    // ---------- POST /api/training-plans/save ----------

    @Test
    @DisplayName("POST /api/training-plans/save -> 200 crea y devuelve plan")
    void create_ok() throws Exception {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setIdClub(1L);
        dto.setName("Base 10K");
        dto.setContentJson("{\"weeks\":8}");

        TrainingPlan saved = new TrainingPlan();
        saved.setName("Base 10K");
        saved.setContentJson("{\"weeks\":8}");

        when(trainingPlanService.save(eq("admin-1"), any(TrainingPlanDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/training-plans/save")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Base 10K"))
                .andExpect(jsonPath("$.contentJson").value("{\"weeks\":8}"));

        verify(trainingPlanService).save(eq("admin-1"), any(TrainingPlanDto.class));
    }

    @Test
    @DisplayName("POST /api/training-plans/save -> 403 si no es admin/club-admin")
    void create_forbidden() throws Exception {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setIdClub(1L);
        dto.setName("Plan X");

        when(trainingPlanService.save(eq("user-1"), any(TrainingPlanDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be admin or club-administrator"));

        mockMvc.perform(post("/api/training-plans/save")
                        .param("uid", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("club-administrator")));
    }

    // ---------- GET /api/training-plans/by-club ----------

    @Test
    @DisplayName("GET /api/training-plans/by-club?idClub=7 -> 200 y lista")
    void getByClub_ok() throws Exception {
        TrainingPlan p1 = new TrainingPlan(); p1.setName("Plan A");
        TrainingPlan p2 = new TrainingPlan(); p2.setName("Plan B");
        when(trainingPlanService.findByClubId(7L)).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/training-plans/by-club").param("idClub", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(trainingPlanService).findByClubId(7L);
    }

    @Test
    @DisplayName("GET /api/training-plans/by-club -> 404 si club no existe")
    void getByClub_notFound() throws Exception {
        when(trainingPlanService.findByClubId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        mockMvc.perform(get("/api/training-plans/by-club").param("idClub", "99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Club not found")));
    }

    // ---------- GET /api/training-plans/all ----------

    @Test
    @DisplayName("GET /api/training-plans/all -> 200 y lista vacía")
    void getAll_ok() throws Exception {
        when(trainingPlanService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/training-plans/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trainingPlanService).findAll();
    }

    // ---------- GET /api/training-plans/getById ----------

    @Test
    @DisplayName("GET /api/training-plans/getById?id=5 -> 200 y devuelve plan")
    void getById_ok() throws Exception {
        TrainingPlan plan = new TrainingPlan();
        plan.setName("Plan Detalle");
        when(trainingPlanService.getById(5L)).thenReturn(plan);

        mockMvc.perform(get("/api/training-plans/getById").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Plan Detalle"));

        verify(trainingPlanService).getById(5L);
    }

    @Test
    @DisplayName("GET /api/training-plans/getById -> 404 si no existe")
    void getById_notFound() throws Exception {
        when(trainingPlanService.getById(77L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        mockMvc.perform(get("/api/training-plans/getById").param("id", "77"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Training plan not found")));
    }

    // ---------- PUT /api/training-plans/update ----------

    @Test
    @DisplayName("PUT /api/training-plans/update -> 200 y devuelve plan actualizado")
    void update_ok() throws Exception {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setName("Plan Editado");
        dto.setContentJson("{\"weeks\":12}");

        TrainingPlan updated = new TrainingPlan();
        updated.setName("Plan Editado");
        updated.setContentJson("{\"weeks\":12}");

        when(trainingPlanService.update(eq("admin-1"), eq(9L), any(TrainingPlanDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/training-plans/update")
                        .param("id", "9")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Plan Editado"))
                .andExpect(jsonPath("$.contentJson").value("{\"weeks\":12}"));

        verify(trainingPlanService).update(eq("admin-1"), eq(9L), any(TrainingPlanDto.class));
    }

    @Test
    @DisplayName("PUT /api/training-plans/update -> 404 si plan no existe")
    void update_notFound() throws Exception {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setName("X");

        when(trainingPlanService.update(eq("admin-1"), eq(999L), any(TrainingPlanDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        mockMvc.perform(put("/api/training-plans/update")
                        .param("id", "999")
                        .param("uid", "admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Training plan not found")));
    }

    @Test
    @DisplayName("PUT /api/training-plans/update -> 403 si no es admin/club-admin")
    void update_forbidden() throws Exception {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setName("X");

        when(trainingPlanService.update(eq("user-1"), eq(5L), any(TrainingPlanDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be admin or club-administrator"));

        mockMvc.perform(put("/api/training-plans/update")
                        .param("id", "5")
                        .param("uid", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("club-administrator")));
    }

    // ---------- DELETE /api/training-plans/delete ----------

    @Test
    @DisplayName("DELETE /api/training-plans/delete -> 204 No Content")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/training-plans/delete")
                        .param("id", "8")
                        .param("uid", "admin-1"))
                .andExpect(status().isNoContent());

        verify(trainingPlanService).delete("admin-1", 8L);
    }

    @Test
    @DisplayName("DELETE /api/training-plans/delete -> 403 si no es admin/club-admin")
    void delete_forbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be admin or club-administrator"))
                .when(trainingPlanService).delete("user-1", 8L);

        mockMvc.perform(delete("/api/training-plans/delete")
                        .param("id", "8")
                        .param("uid", "user-1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("club-administrator")));
    }
}
