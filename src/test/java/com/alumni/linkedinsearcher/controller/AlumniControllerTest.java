package com.alumni.linkedinsearcher.controller;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.exception.GlobalExceptionHandler;
import com.alumni.linkedinsearcher.exception.PhantomBusterException;
import com.alumni.linkedinsearcher.service.AlumniService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlumniControllerTest {

    @Mock
    private AlumniService alumniService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AlumniController(alumniService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchReturnsSuccessEnvelopeWithResults() throws Exception {
        AlumniResponseDto dto = AlumniResponseDto.builder()
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Passionate Software Engineer at XYZ Corp")
                .passoutYear(2020)
                .build();

        when(alumniService.searchAlumni(any())).thenReturn(List.of(dto));

        Map<String, Object> body = Map.of(
                "university", "University of XYZ",
                "designation", "Software Engineer",
                "passoutYear", 2020);

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data[0].university").value("University of XYZ"));
    }

    @Test
    void searchReturns400WhenUniversityIsBlank() throws Exception {
        Map<String, Object> body = Map.of("university", "", "designation", "Software Engineer");

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void searchReturns502WhenPhantomBusterFails() throws Exception {
        when(alumniService.searchAlumni(any())).thenThrow(new PhantomBusterException("PhantomBuster is down"));

        Map<String, Object> body = Map.of("university", "University of XYZ", "designation", "Software Engineer");

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("PhantomBuster is down"));
    }

    @Test
    void getAllReturnsSavedAlumni() throws Exception {
        AlumniResponseDto dto = AlumniResponseDto.builder().name("Jane Smith").university("University of XYZ").build();
        when(alumniService.getAllAlumni()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/alumni/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("Jane Smith"));
    }
}
