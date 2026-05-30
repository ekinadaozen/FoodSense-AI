package com.foodsense.ai.service;

import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.entity.Complaint;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.kafka.ComplaintProducer;
import com.foodsense.ai.repository.ComplaintRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintService Unit Tests")
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintProducer complaintProducer;

    @InjectMocks
    private ComplaintService complaintService;

    @Captor
    private ArgumentCaptor<Complaint> complaintCaptor;

    // ── helpers ──────────────────────────────────────────────────────────────

    private ComplaintRequestDto buildRequest(String name, String text) {
        return ComplaintRequestDto.builder()
                .customerName(name)
                .complaintText(text)
                .build();
    }

    private Complaint buildComplaint(UUID id, String name, String text) {
        return Complaint.builder()
                .id(id)
                .customerName(name)
                .complaintText(text)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── submitComplaint ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("submitComplaint")
    class SubmitComplaint {

        @Test
        @DisplayName("Should save complaint and publish to Kafka, returning correct response DTO")
        void submitComplaint_ShouldSaveAndPublish() {
            // Arrange
            UUID id = UUID.randomUUID();
            String name = "Alice Johnson";
            String text = "My order arrived cold and 45 minutes late.";
            ComplaintRequestDto request = buildRequest(name, text);

            Complaint savedEntity = buildComplaint(id, name, text);

            given(complaintRepository.save(any(Complaint.class))).willReturn(savedEntity);

            // Act
            ComplaintResponseDto response = complaintService.submitComplaint(request);

            // Assert – response fields
            assertAll("response DTO fields",
                    () -> assertThat(response.getId()).isEqualTo(id),
                    () -> assertThat(response.getCustomerName()).isEqualTo(name),
                    () -> assertThat(response.getComplaintText()).isEqualTo(text),
                    () -> assertThat(response.getCreatedAt()).isNotNull()
            );

            // Assert – repository interaction
            verify(complaintRepository).save(complaintCaptor.capture());
            Complaint captured = complaintCaptor.getValue();
            assertThat(captured.getCustomerName()).isEqualTo(name);
            assertThat(captured.getComplaintText()).isEqualTo(text);

            // Assert – Kafka producer called with saved complaint id
            verify(complaintProducer).sendComplaint(id.toString());
        }

        @Test
        @DisplayName("Should propagate repository exception without catching it")
        void submitComplaint_ShouldPropagateRepositoryException() {
            ComplaintRequestDto request = buildRequest("Bob", "Some valid complaint text here.");

            given(complaintRepository.save(any(Complaint.class)))
                    .willThrow(new RuntimeException("DB unavailable"));

            assertThatThrownBy(() -> complaintService.submitComplaint(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB unavailable");

            verify(complaintProducer, never()).sendComplaint(anyString());
        }
    }

    // ── getComplaintById ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getComplaintById")
    class GetComplaintById {

        @Test
        @DisplayName("Should return complaint response when complaint exists")
        void getComplaintById_ShouldReturnComplaint() {
            UUID id = UUID.randomUUID();
            Complaint complaint = buildComplaint(id, "Charlie", "Order was wrong.");

            given(complaintRepository.findById(id)).willReturn(Optional.of(complaint));

            ComplaintResponseDto response = complaintService.getComplaintById(id);

            assertAll("response matches entity",
                    () -> assertThat(response.getId()).isEqualTo(id),
                    () -> assertThat(response.getCustomerName()).isEqualTo("Charlie"),
                    () -> assertThat(response.getComplaintText()).isEqualTo("Order was wrong.")
            );

            verify(complaintRepository).findById(id);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when complaint does not exist")
        void getComplaintById_ShouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();

            given(complaintRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> complaintService.getComplaintById(id))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(complaintRepository).findById(id);
        }
    }

    // ── getAllComplaints ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllComplaints")
    class GetAllComplaints {

        @Test
        @DisplayName("Should return list of complaint response DTOs ordered by createdAt desc")
        void getAllComplaints_ShouldReturnList() {
            Complaint c1 = buildComplaint(UUID.randomUUID(), "Dave", "Late delivery again.");
            Complaint c2 = buildComplaint(UUID.randomUUID(), "Eve", "Payment was charged twice.");

            given(complaintRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(c1, c2));

            List<ComplaintResponseDto> result = complaintService.getAllComplaints();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCustomerName()).isEqualTo("Dave");
            assertThat(result.get(1).getCustomerName()).isEqualTo("Eve");

            verify(complaintRepository).findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("Should return empty list when no complaints exist")
        void getAllComplaints_ShouldReturnEmptyList() {
            given(complaintRepository.findAllByOrderByCreatedAtDesc())
                    .willReturn(Collections.emptyList());

            List<ComplaintResponseDto> result = complaintService.getAllComplaints();

            assertThat(result).isEmpty();
            verify(complaintRepository).findAllByOrderByCreatedAtDesc();
        }
    }
}
