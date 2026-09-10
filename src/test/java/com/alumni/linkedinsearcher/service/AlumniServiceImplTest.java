package com.alumni.linkedinsearcher.service;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.entity.Alumni;
import com.alumni.linkedinsearcher.mapper.AlumniMapper;
import com.alumni.linkedinsearcher.phantombuster.PhantomBusterClient;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import com.alumni.linkedinsearcher.repository.AlumniRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumniServiceImplTest {

    @Mock
    private PhantomBusterClient phantomBusterClient;

    @Mock
    private AlumniRepository alumniRepository;

    private final AlumniMapper alumniMapper = new AlumniMapper();

    private AlumniServiceImpl alumniService;

    private AlumniSearchRequest request;

    @BeforeEach
    void setUp() {
        request = new AlumniSearchRequest("University of XYZ", "Software Engineer", 2020);
        alumniService = new AlumniServiceImpl(phantomBusterClient, alumniRepository, alumniMapper);
    }

    @Test
    void searchAlumniScrapesMapsAndPersistsNewProfiles() {
        PhantomBusterProfileDto scraped = new PhantomBusterProfileDto();
        scraped.setFullName("John Doe");
        scraped.setJobTitle("Software Engineer");
        scraped.setLocation("New York, NY");
        scraped.setProfileUrl("https://linkedin.com/in/johndoe");

        when(phantomBusterClient.searchLinkedInProfiles(request)).thenReturn(List.of(scraped));
        when(alumniRepository.findByProfileUrl("https://linkedin.com/in/johndoe")).thenReturn(Optional.empty());
        when(alumniRepository.save(any(Alumni.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlumniResponseDto> results = alumniService.searchAlumni(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John Doe");
        assertThat(results.get(0).getUniversity()).isEqualTo("University of XYZ");
        verify(alumniRepository, times(1)).save(any(Alumni.class));
    }

    @Test
    void searchAlumniUpdatesExistingProfileInsteadOfDuplicating() {
        PhantomBusterProfileDto scraped = new PhantomBusterProfileDto();
        scraped.setFullName("Jane Smith");
        scraped.setProfileUrl("https://linkedin.com/in/janesmith");

        Alumni existing = Alumni.builder().id(42L).profileUrl("https://linkedin.com/in/janesmith").name("Jane").build();

        when(phantomBusterClient.searchLinkedInProfiles(request)).thenReturn(List.of(scraped));
        when(alumniRepository.findByProfileUrl("https://linkedin.com/in/janesmith")).thenReturn(Optional.of(existing));
        when(alumniRepository.save(any(Alumni.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlumniResponseDto> results = alumniService.searchAlumni(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Jane Smith");
        verify(alumniRepository, times(1)).save(existing);
    }

    @Test
    void searchAlumniReturnsEmptyListWhenNoProfilesFound() {
        when(phantomBusterClient.searchLinkedInProfiles(request)).thenReturn(List.of());

        List<AlumniResponseDto> results = alumniService.searchAlumni(request);

        assertThat(results).isEmpty();
        verify(alumniRepository, never()).save(any());
    }

    @Test
    void getAllAlumniReturnsAllSavedRecords() {
        Alumni alumni = Alumni.builder().name("John Doe").university("University of XYZ").build();
        when(alumniRepository.findAll()).thenReturn(List.of(alumni));

        List<AlumniResponseDto> results = alumniService.getAllAlumni();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John Doe");
    }
}
