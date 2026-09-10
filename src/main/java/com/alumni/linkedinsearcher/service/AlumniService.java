package com.alumni.linkedinsearcher.service;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;

import java.util.List;

public interface AlumniService {

    List<AlumniResponseDto> searchAlumni(AlumniSearchRequest request);

    List<AlumniResponseDto> getAllAlumni();
}
