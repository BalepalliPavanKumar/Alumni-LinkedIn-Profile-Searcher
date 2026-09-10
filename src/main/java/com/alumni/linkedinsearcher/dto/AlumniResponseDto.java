package com.alumni.linkedinsearcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniResponseDto {
    private String name;
    private String currentRole;
    private String university;
    private String location;
    private String linkedinHeadline;
    private String profileUrl;
    private Integer passoutYear;
}
