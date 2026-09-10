package com.alumni.linkedinsearcher.mapper;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.entity.Alumni;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlumniMapperTest {

    private final AlumniMapper mapper = new AlumniMapper();

    @Test
    void mapsPhantomBusterProfileToEntityUsingRequestAsFallback() {
        AlumniSearchRequest request = new AlumniSearchRequest("University of XYZ", "Software Engineer", 2021);

        PhantomBusterProfileDto profile = new PhantomBusterProfileDto();
        profile.setFullName("Jane Smith");
        profile.setHeadline("Data Scientist | AI Enthusiast");
        profile.setLocation("San Francisco, CA");
        profile.setProfileUrl("https://linkedin.com/in/janesmith");
        // jobTitle and graduationYear intentionally left null to exercise fallback to the request

        Alumni alumni = mapper.toEntity(profile, request);

        assertThat(alumni.getName()).isEqualTo("Jane Smith");
        assertThat(alumni.getCurrentRole()).isEqualTo("Software Engineer");
        assertThat(alumni.getUniversity()).isEqualTo("University of XYZ");
        assertThat(alumni.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(alumni.getLinkedinHeadline()).isEqualTo("Data Scientist | AI Enthusiast");
        assertThat(alumni.getProfileUrl()).isEqualTo("https://linkedin.com/in/janesmith");
        assertThat(alumni.getPassoutYear()).isEqualTo(2021);
    }

    @Test
    void prefersScrapedJobTitleAndGraduationYearOverRequest() {
        AlumniSearchRequest request = new AlumniSearchRequest("University of XYZ", "Software Engineer", 2021);

        PhantomBusterProfileDto profile = new PhantomBusterProfileDto();
        profile.setFullName("John Doe");
        profile.setJobTitle("Senior Software Engineer");
        profile.setGraduationYear(2019);

        Alumni alumni = mapper.toEntity(profile, request);

        assertThat(alumni.getCurrentRole()).isEqualTo("Senior Software Engineer");
        assertThat(alumni.getPassoutYear()).isEqualTo(2019);
    }

    @Test
    void mapsEntityToResponseDto() {
        Alumni alumni = Alumni.builder()
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Passionate Software Engineer at XYZ Corp")
                .profileUrl("https://linkedin.com/in/johndoe")
                .passoutYear(2020)
                .build();

        AlumniResponseDto dto = mapper.toResponseDto(alumni);

        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getCurrentRole()).isEqualTo("Software Engineer");
        assertThat(dto.getUniversity()).isEqualTo("University of XYZ");
        assertThat(dto.getLocation()).isEqualTo("New York, NY");
        assertThat(dto.getLinkedinHeadline()).isEqualTo("Passionate Software Engineer at XYZ Corp");
        assertThat(dto.getProfileUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        assertThat(dto.getPassoutYear()).isEqualTo(2020);
    }
}
