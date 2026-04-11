package com.sentinel.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.api.service.SessionStateService;
import com.sentinel.common.domain.dto.request.DraftSaveRequest;
import com.sentinel.common.domain.entity.DraftState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DraftController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignore JWT filter for unit tests
class DraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionStateService sessionStateService;

    private DraftState mockDraft;
    private DraftSaveRequest saveRequest;

    @BeforeEach
    void setUp() {
        mockDraft = new DraftState("usr-999", "{\"data\": \"draft content\"}");
        
        saveRequest = new DraftSaveRequest();
        saveRequest.setPayload("{\"data\": \"draft content\"}");
    }

    @Test
    void testSaveDraft_Success() throws Exception {
        Mockito.when(sessionStateService.saveDraft(anyString(), anyString())).thenReturn(mockDraft);

        mockMvc.perform(post("/api/draft")
                        .with(user("usr-999").roles("USER")) // Set mock principal to return from SecurityContext
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("usr-999"))
                .andExpect(jsonPath("$.draftPayload").value("{\"data\": \"draft content\"}"));
    }

    @Test
    void testGetDraft_WhenExists_ReturnsDraft() throws Exception {
        Mockito.when(sessionStateService.getDraft("usr-999")).thenReturn(Optional.of(mockDraft));

        mockMvc.perform(get("/api/draft")
                        .with(user("usr-999").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("usr-999"))
                .andExpect(jsonPath("$.draftPayload").value("{\"data\": \"draft content\"}"));
    }

    @Test
    void testGetDraft_WhenNotExists_ReturnsMessage() throws Exception {
        Mockito.when(sessionStateService.getDraft("usr-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/draft")
                        .with(user("usr-999").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No draft found for user"));
    }
}
