package com.foodsense.ai.controller;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.service.AnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalysisController.class)
@DisplayName("AnalysisController Integration Tests")
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    private static final String BASE_URL = "/api/analyses";

    // ── helpers ──────────────────────────────────────────────────────────────

    private AnalysisResponseDto buildResponse(UUID id, UUID complaintId) {
        return AnalysisResponseDto.builder()
                .id(id)
                .complaintId(complaintId)
                .category("FOOD_QUALITY")
                .sentiment("NEGATIVE")
                .priority("HIGH")
                .summary("Customer reports cold food delivered late.")
                .analyzedAt(LocalDateTime.of(2026, 6, 1, 12, 0, 0))
                .build();
    }

    // ── GET /api/analyses ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/analyses")
    class GetAllAnalyses {

        @Test
        @DisplayName("Should return 200 OK with list of analyses")
        void getAllAnalyses_ShouldReturn200() throws Exception {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID complaintId1 = UUID.randomUUID();
            UUID complaintId2 = UUID.randomUUID();

            List<AnalysisResponseDto> list = List.of(
                    buildResponse(id1, complaintId1),
                    buildResponse(id2, complaintId2)
            );

            given(analysisService.getAllAnalyses()).willReturn(list);

            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(id1.toString()))
                    .andExpect(jsonPath("$[0].complaintId").value(complaintId1.toString()))
                    .andExpect(jsonPath("$[0].category").value("FOOD_QUALITY"))
                    .andExpect(jsonPath("$[0].sentiment").value("NEGATIVE"))
                    .andExpect(jsonPath("$[0].priority").value("HIGH"))
                    .andExpect(jsonPath("$[0].summary").value("Customer reports cold food delivered late."))
                    .andExpect(jsonPath("$[1].id").value(id2.toString()));

            verify(analysisService).getAllAnalyses();
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no analyses exist")
        void getAllAnalyses_ShouldReturn200WithEmptyList() throws Exception {
            given(analysisService.getAllAnalyses()).willReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ── GET /api/analyses/complaint/{complaintId} ───────────────────────────

    @Nested
    @DisplayName("GET /api/analyses/complaint/{complaintId}")
    class GetAnalysisByComplaintId {

        @Test
        @DisplayName("Should return 200 OK when analysis exists for complaint")
        void getAnalysisByComplaintId_ShouldReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            UUID complaintId = UUID.randomUUID();
            AnalysisResponseDto response = buildResponse(id, complaintId);

            given(analysisService.getAnalysisByComplaintId(complaintId)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/complaint/{complaintId}", complaintId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.complaintId").value(complaintId.toString()))
                    .andExpect(jsonPath("$.category").value("FOOD_QUALITY"))
                    .andExpect(jsonPath("$.sentiment").value("NEGATIVE"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.summary").value("Customer reports cold food delivered late."));

            verify(analysisService).getAnalysisByComplaintId(complaintId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when no analysis exists for complaint")
        void getAnalysisByComplaintId_ShouldReturn404() throws Exception {
            UUID complaintId = UUID.randomUUID();

            given(analysisService.getAnalysisByComplaintId(complaintId))
                    .willThrow(new ResourceNotFoundException("ComplaintAnalysis", "complaintId", complaintId));

            mockMvc.perform(get(BASE_URL + "/complaint/{complaintId}", complaintId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(analysisService).getAnalysisByComplaintId(complaintId);
        }
    }
}
