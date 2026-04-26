package com.sentinel.api.service;

import com.sentinel.api.repository.DraftStateRepository;
import com.sentinel.common.domain.entity.DraftState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionStateServiceTest {

    @Mock
    private DraftStateRepository repository;

    @InjectMocks
    private SessionStateService sessionStateService;

    private DraftState draft;

    @BeforeEach
    void setUp() {
        draft = new DraftState("user123", "{\"text\": \"draft data\"}");
    }

    @Test
    void testSaveDraft_WhenExisting_UpdatesAndSaves() {
        // Arrange
        when(repository.findByUserId("user123")).thenReturn(Optional.of(draft));
        when(repository.save(any(DraftState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DraftState result = sessionStateService.saveDraft("user123", "{\"text\": \"new draft data\"}");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals("{\"text\": \"new draft data\"}", result.getDraftPayload());
        assertNotNull(result.getUpdatedAt());
        verify(repository, times(1)).findByUserId("user123");
        verify(repository, times(1)).save(draft);
    }

    @Test
    void testSaveDraft_WhenNotExisting_CreatesAndSaves() {
        // Arrange
        when(repository.findByUserId("user123")).thenReturn(Optional.empty());
        when(repository.save(any(DraftState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DraftState result = sessionStateService.saveDraft("user123", "{\"text\": \"draft data\"}");

        // Assert
        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals("{\"text\": \"draft data\"}", result.getDraftPayload());
        verify(repository, times(1)).findByUserId("user123");
        verify(repository, times(1)).save(any(DraftState.class));
    }

    @Test
    void testGetDraft_WhenExists_ReturnsDraft() {
        // Arrange
        when(repository.findByUserId("user123")).thenReturn(Optional.of(draft));

        // Act
        Optional<DraftState> result = sessionStateService.getDraft("user123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("user123", result.get().getUserId());
    }

    @Test
    void testGetDraft_WhenNotExists_ReturnsEmpty() {
        // Arrange
        when(repository.findByUserId("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<DraftState> result = sessionStateService.getDraft("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }
}
