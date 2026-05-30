package com.foodsense.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.service.ComplaintService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplaintController.class)
@DisplayName("ComplaintController Integration Tests")
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    private static final String BASE_URL = "/api/complaints";

    // ── helpers ──────────────────────────────────────────────────────────────

    private ComplaintResponseDto buildResponse(UUID id, String name, String text) {
        return ComplaintResponseDto.builder()
                .id(id)
                .customerName(name)
                .complaintText(text)
                .createdAt(LocalDateTime.of(2026, 5, 30, 12, 0, 0))
                .build();
    }

    // ── POST /api/complaints ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/complaints")
    class SubmitComplaint {

        @Test
        @DisplayName("Should return 201 Created with valid complaint payload")
        void submitComplaint_ShouldReturn201() throws Exception {
            UUID id = UUID.randomUUID();
            String name = "Alice Johnson";
            String text = "My order arrived cold and was missing items.";

            ComplaintRequestDto request = ComplaintRequestDto.builder()
                    .customerName(name)
                    .complaintText(text)
                    .build();

            ComplaintResponseDto response = buildResponse(id, name, text);
            given(complaintService.submitComplaint(any(ComplaintRequestDto.class))).willReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.customerName").value(name))
                    .andExpect(jsonPath("$.complaintText").value(text))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());

            verify(complaintService).submitComplaint(any(ComplaintRequestDto.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when customerName is blank")
        void submitComplaint_ShouldReturn400ForBlankCustomerName() throws Exception {
            ComplaintRequestDto request = ComplaintRequestDto.builder()
                    .customerName("")
                    .complaintText("This is a valid complaint text with enough characters.")
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(complaintService, never()).submitComplaint(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when complaintText is too short")
        void submitComplaint_ShouldReturn400ForShortComplaintText() throws Exception {
            ComplaintRequestDto request = ComplaintRequestDto.builder()
                    .customerName("Bob Smith")
                    .complaintText("Short")    // less than 10 chars
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(complaintService, never()).submitComplaint(any());
        }

        @Test
        @DisplayName("Should return 400 Bad Request when complaintText is missing")
        void submitComplaint_ShouldReturn400ForMissingComplaintText() throws Exception {
            String json = """
                    { "customerName": "Carol" }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(complaintService, never()).submitComplaint(any());
        }
    }

    // ── GET /api/complaints ─────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/complaints")
    class GetAllComplaints {

        @Test
        @DisplayName("Should return 200 OK with list of complaints")
        void getAllComplaints_ShouldReturn200() throws Exception {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<ComplaintResponseDto> list = List.of(
                    buildResponse(id1, "Dave", "Delivery was very late and food was cold."),
                    buildResponse(id2, "Eve", "I was charged twice for my order.")
            );

            given(complaintService.getAllComplaints()).willReturn(list);

            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].customerName").value("Dave"))
                    .andExpect(jsonPath("$[1].customerName").value("Eve"));

            verify(complaintService).getAllComplaints();
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no complaints exist")
        void getAllComplaints_ShouldReturn200WithEmptyList() throws Exception {
            given(complaintService.getAllComplaints()).willReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ── GET /api/complaints/{id} ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/complaints/{id}")
    class GetComplaintById {

        @Test
        @DisplayName("Should return 200 OK when complaint exists")
        void getComplaintById_ShouldReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            ComplaintResponseDto response = buildResponse(id, "Frank", "App keeps crashing on checkout.");

            given(complaintService.getComplaintById(id)).willReturn(response);

            mockMvc.perform(get(BASE_URL + "/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.customerName").value("Frank"))
                    .andExpect(jsonPath("$.complaintText").value("App keeps crashing on checkout."));

            verify(complaintService).getComplaintById(id);
        }

        @Test
        @DisplayName("Should return 404 Not Found when complaint does not exist")
        void getComplaintById_ShouldReturn404() throws Exception {
            UUID id = UUID.randomUUID();

            given(complaintService.getComplaintById(id))
                    .willThrow(new ResourceNotFoundException("Complaint", "id", id));

            mockMvc.perform(get(BASE_URL + "/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(complaintService).getComplaintById(id);
        }
    }
}
