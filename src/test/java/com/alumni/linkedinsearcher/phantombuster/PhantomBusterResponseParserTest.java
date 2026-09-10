package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.exception.PhantomBusterException;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhantomBusterResponseParserTest {

    private final PhantomBusterResponseParser parser = new PhantomBusterResponseParser();

    @Test
    void parsesResultObjectJsonIntoProfileDtos() {
        String json = """
                [
                  {
                    "fullName": "John Doe",
                    "title": "Software Engineer at XYZ Corp",
                    "jobTitle": "Software Engineer",
                    "location": "New York, NY",
                    "profileUrl": "https://linkedin.com/in/johndoe",
                    "school": "University of XYZ",
                    "graduationYear": 2020
                  }
                ]
                """;

        List<PhantomBusterProfileDto> profiles = parser.parse(json);

        assertThat(profiles).hasSize(1);
        PhantomBusterProfileDto profile = profiles.get(0);
        assertThat(profile.getFullName()).isEqualTo("John Doe");
        assertThat(profile.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(profile.getLocation()).isEqualTo("New York, NY");
        assertThat(profile.getProfileUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        assertThat(profile.getGraduationYear()).isEqualTo(2020);
    }

    @Test
    void returnsEmptyListForBlankInput() {
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    void throwsPhantomBusterExceptionForMalformedJson() {
        assertThatThrownBy(() -> parser.parse("not-json"))
                .isInstanceOf(PhantomBusterException.class);
    }
}
