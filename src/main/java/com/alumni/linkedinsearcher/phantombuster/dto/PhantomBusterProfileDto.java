package com.alumni.linkedinsearcher.phantombuster.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Raw shape of a single result produced by PhantomBuster's LinkedIn search /
 * profile scraper phantoms. Field names are aliased because different
 * PhantomBuster phantoms (Search Export, Profile Scraper, Sales Navigator
 * Search Export, ...) name these slightly differently in their result JSON.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhantomBusterProfileDto {

    @JsonAlias({"fullName", "name"})
    private String fullName;

    @JsonAlias({"job", "title", "headline", "linkedinHeadline"})
    private String headline;

    @JsonAlias({"jobTitle", "currentRole", "company"})
    private String jobTitle;

    @JsonAlias({"location", "city"})
    private String location;

    @JsonAlias({"profileUrl", "linkedinProfileUrl", "url"})
    private String profileUrl;

    @JsonAlias({"school", "university"})
    private String school;

    @JsonAlias({"educationEndDate", "passoutYear", "graduationYear"})
    private Integer graduationYear;
}
