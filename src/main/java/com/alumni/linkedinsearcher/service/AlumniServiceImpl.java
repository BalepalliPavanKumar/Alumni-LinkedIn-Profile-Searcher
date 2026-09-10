package com.alumni.linkedinsearcher.service;

import com.alumni.linkedinsearcher.dto.AlumniResponseDto;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.entity.Alumni;
import com.alumni.linkedinsearcher.mapper.AlumniMapper;
import com.alumni.linkedinsearcher.phantombuster.PhantomBusterClient;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import com.alumni.linkedinsearcher.repository.AlumniRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlumniServiceImpl implements AlumniService {

    private final PhantomBusterClient phantomBusterClient;
    private final AlumniRepository alumniRepository;
    private final AlumniMapper alumniMapper;

    public AlumniServiceImpl(PhantomBusterClient phantomBusterClient,
                              AlumniRepository alumniRepository,
                              AlumniMapper alumniMapper) {
        this.phantomBusterClient = phantomBusterClient;
        this.alumniRepository = alumniRepository;
        this.alumniMapper = alumniMapper;
    }

    @Override
    @Transactional
    public List<AlumniResponseDto> searchAlumni(AlumniSearchRequest request) {
        List<PhantomBusterProfileDto> scrapedProfiles = phantomBusterClient.searchLinkedInProfiles(request);

        return scrapedProfiles.stream()
                .map(profile -> alumniMapper.toEntity(profile, request))
                .map(this::saveOrUpdate)
                .map(alumniMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniResponseDto> getAllAlumni() {
        return alumniRepository.findAll().stream()
                .map(alumniMapper::toResponseDto)
                .toList();
    }

    /** Avoids duplicate rows when the same LinkedIn profile is scraped across repeated searches. */
    private Alumni saveOrUpdate(Alumni incoming) {
        if (incoming.getProfileUrl() == null || incoming.getProfileUrl().isBlank()) {
            return alumniRepository.save(incoming);
        }

        return alumniRepository.findByProfileUrl(incoming.getProfileUrl())
                .map(existing -> {
                    existing.setName(incoming.getName());
                    existing.setCurrentRole(incoming.getCurrentRole());
                    existing.setUniversity(incoming.getUniversity());
                    existing.setLocation(incoming.getLocation());
                    existing.setLinkedinHeadline(incoming.getLinkedinHeadline());
                    existing.setPassoutYear(incoming.getPassoutYear());
                    return alumniRepository.save(existing);
                })
                .orElseGet(() -> alumniRepository.save(incoming));
    }
}
