package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the "argument" payload PhantomBuster expects when launching a
 * LinkedIn search phantom, from the university/designation/passoutYear
 * search criteria the API receives.
 */
public class LinkedInSearchQueryBuilder {

    private String university;
    private String designation;
    private Integer passoutYear;
    private int resultLimit = 20;

    public static LinkedInSearchQueryBuilder builder() {
        return new LinkedInSearchQueryBuilder();
    }

    public LinkedInSearchQueryBuilder university(String university) {
        this.university = university;
        return this;
    }

    public LinkedInSearchQueryBuilder designation(String designation) {
        this.designation = designation;
        return this;
    }

    public LinkedInSearchQueryBuilder passoutYear(Integer passoutYear) {
        this.passoutYear = passoutYear;
        return this;
    }

    public LinkedInSearchQueryBuilder resultLimit(int resultLimit) {
        this.resultLimit = resultLimit;
        return this;
    }

    public static LinkedInSearchQueryBuilder fromRequest(AlumniSearchRequest request) {
        return builder()
                .university(request.getUniversity())
                .designation(request.getDesignation())
                .passoutYear(request.getPassoutYear());
    }

    /**
     * Free-text keyword portion of the search: designation and (if given) the
     * passout year. The university is deliberately excluded here - it is
     * applied as a proper structured filter instead (see
     * {@link #buildLaunchArgument()}), not loose keyword text.
     */
    public String buildKeywords() {
        StringBuilder keywords = new StringBuilder();
        keywords.append(designation);
        if (passoutYear != null) {
            keywords.append(' ').append(passoutYear);
        }
        return keywords.toString().trim();
    }

    /**
     * Builds the search-specific fields to override on top of the phantom's
     * saved argument (which also holds the connected LinkedIn identity/session
     * cookie).
     *
     * <p>Uses {@code searchType: "keywords"} plus a {@code resolvablePeopleSearchFilters.schools}
     * filter rather than stuffing the university name into free-text keywords:
     * PhantomBuster resolves that school name to LinkedIn's actual school
     * entity and applies it as a real facet filter, so results are people who
     * actually attended that university - not just anyone whose profile
     * happens to mention the name.
     */
    public Map<String, Object> buildLaunchArgument() {
        Map<String, Object> argument = new LinkedHashMap<>();
        argument.put("searchType", "keywords");
        argument.put("keywords", buildKeywords());
        argument.put("resolvablePeopleSearchFilters", Map.of("schools", List.of(university)));
        argument.put("numberOfResultsPerLaunch", resultLimit);
        argument.put("numberOfResultsPerSearch", resultLimit);
        return argument;
    }
}
