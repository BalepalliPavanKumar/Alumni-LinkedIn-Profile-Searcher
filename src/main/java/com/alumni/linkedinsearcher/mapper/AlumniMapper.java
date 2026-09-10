package com.alumni.linkedinsearcher.mapper;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.entity.Alumni;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import org.springframework.stereotype.Component;

@Component
public class AlumniMapper {

    public Alumni toEntity(PhantomBusterProfileDto profile, AlumniSearchRequest request) {
        return Alumni.builder()
                .name(profile.getFullName())
                .currentRole(resolveCurrentRole(profile, request))
                .university(request.getUniversity())
                .location(profile.getLocation())
                .linkedinHeadline(profile.getHeadline())
                .profileUrl(profile.getProfileUrl())
                .passoutYear(profile.getGraduationYear() != null ? profile.getGraduationYear() : request.getPassoutYear())
                .build();
    }

    public AlumniResponseDto toResponseDto(Alumni alumni) {
        return AlumniResponseDto.builder()
                .name(alumni.getName())
                .currentRole(alumni.getCurrentRole())
                .university(alumni.getUniversity())
                .location(alumni.getLocation())
                .linkedinHeadline(alumni.getLinkedinHeadline())
                .profileUrl(alumni.getProfileUrl())
                .passoutYear(alumni.getPassoutYear())
                .build();
    }

    private String resolveCurrentRole(PhantomBusterProfileDto profile, AlumniSearchRequest request) {
        return profile.getJobTitle() != null && !profile.getJobTitle().isBlank()
                ? profile.getJobTitle()
                : request.getDesignation();
    }
}
