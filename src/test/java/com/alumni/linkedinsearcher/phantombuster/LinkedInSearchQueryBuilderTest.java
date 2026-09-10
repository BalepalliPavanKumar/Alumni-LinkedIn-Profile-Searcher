package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LinkedInSearchQueryBuilderTest {

    @Test
    void buildsKeywordsFromDesignationAndPassoutYearOnly() {
        String keywords = LinkedInSearchQueryBuilder.builder()
                .designation("Software Engineer")
                .university("XYZ University")
                .passoutYear(2020)
                .buildKeywords();

        // University is deliberately excluded from free-text keywords - it's
        // applied as a structured school filter instead, not loose text.
        assertThat(keywords).isEqualTo("Software Engineer 2020");
    }

    @Test
    void omitsPassoutYearWhenNotProvided() {
        String keywords = LinkedInSearchQueryBuilder.builder()
                .designation("Data Scientist")
                .university("ABC College")
                .buildKeywords();

        assertThat(keywords).isEqualTo("Data Scientist");
    }

    @Test
    void launchArgumentUsesKeywordsSearchTypeWithSchoolFilterAndResultLimit() {
        AlumniSearchRequest request = new AlumniSearchRequest("XYZ University", "Software Engineer", 2020);

        Map<String, Object> argument = LinkedInSearchQueryBuilder.fromRequest(request)
                .resultLimit(15)
                .buildLaunchArgument();

        assertThat(argument.get("searchType")).isEqualTo("keywords");
        assertThat(argument.get("keywords")).isEqualTo("Software Engineer 2020");
        assertThat(argument.get("resolvablePeopleSearchFilters"))
                .isEqualTo(Map.of("schools", List.of("XYZ University")));
        assertThat(argument.get("numberOfResultsPerLaunch")).isEqualTo(15);
        assertThat(argument.get("numberOfResultsPerSearch")).isEqualTo(15);
    }
}
