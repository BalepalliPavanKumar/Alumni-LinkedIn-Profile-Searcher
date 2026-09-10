package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.exception.PhantomBusterException;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Parses the raw "resultObject" JSON string returned by PhantomBuster's
 * fetch-result-object endpoint into typed profile DTOs. Kept separate from
 * the HTTP client so the parsing logic can be unit tested without a network
 * call, and builds its own {@link ObjectMapper} rather than depending on
 * Spring's auto-configured one to avoid any ambiguity between the Jackson 2
 * and Jackson 3 mapper beans Boot exposes side by side.
 */
@Component
public class PhantomBusterResponseParser {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public List<PhantomBusterProfileDto> parse(String rawResultObjectJson) {
        if (rawResultObjectJson == null || rawResultObjectJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    rawResultObjectJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PhantomBusterProfileDto.class));
        } catch (Exception e) {
            throw new PhantomBusterException("Failed to parse PhantomBuster result output", e);
        }
    }
}
