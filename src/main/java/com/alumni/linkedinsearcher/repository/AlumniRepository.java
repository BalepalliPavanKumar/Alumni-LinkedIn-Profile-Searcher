package com.alumni.linkedinsearcher.repository;

import com.alumni.linkedinsearcher.entity.Alumni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {

    Optional<Alumni> findByProfileUrl(String profileUrl);
}
