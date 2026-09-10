package com.alumni.linkedinsearcher.controller;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.dto.ApiResponse;
import com.alumni.linkedinsearcher.service.AlumniService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<AlumniResponseDto>>> searchAlumni(
            @Valid @RequestBody AlumniSearchRequest request) {
        List<AlumniResponseDto> results = alumniService.searchAlumni(request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AlumniResponseDto>>> getAllAlumni() {
        return ResponseEntity.ok(ApiResponse.success(alumniService.getAllAlumni()));
    }
}
