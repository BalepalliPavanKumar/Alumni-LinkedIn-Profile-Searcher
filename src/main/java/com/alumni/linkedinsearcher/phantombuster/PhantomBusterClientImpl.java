package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.config.PhantomBusterProperties;
import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.exception.PhantomBusterException;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default {@link PhantomBusterClient} implementation. Launches the
 * configured LinkedIn search phantom, polls its container until the run
 * finishes, then fetches and parses the scraped result set.
 *
 * <p>Follows PhantomBuster's v2 REST contract:
 * <ul>
 *   <li>{@code POST /agents/launch} to start a run</li>
 *   <li>{@code GET /agents/fetch-output?id=} to poll run status (note: the
 *       container-scoped {@code /containers/fetch-output} only ever returns
 *       an {@code output} field, never a {@code status} - the agent-scoped
 *       endpoint is the one that reports {@code isAgentRunning}/{@code status})</li>
 *   <li>{@code GET /containers/fetch-result-object?id=} to retrieve results</li>
 * </ul>
 */
@Component
public class PhantomBusterClientImpl implements PhantomBusterClient {

    private final WebClient webClient;
    private final PhantomBusterProperties properties;
    private final PhantomBusterResponseParser responseParser;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public PhantomBusterClientImpl(WebClient phantomBusterWebClient,
                                    PhantomBusterProperties properties,
                                    PhantomBusterResponseParser responseParser) {
        this.webClient = phantomBusterWebClient;
        this.properties = properties;
        this.responseParser = responseParser;
    }

    @Override
    public List<PhantomBusterProfileDto> searchLinkedInProfiles(AlumniSearchRequest request) {
        String containerId = launch(request);
        String output = awaitCompletion(containerId);
        failIfRunErrored(containerId, output);
        String rawResult = fetchResultObject(containerId);
        return responseParser.parse(rawResult);
    }

    private String launch(AlumniSearchRequest request) {
        Map<String, Object> overrides = LinkedInSearchQueryBuilder.fromRequest(request)
                .resultLimit(properties.resultLimit())
                .buildLaunchArgument();

        // PhantomBuster's launch API replaces the agent's argument outright rather
        // than merging, so the saved argument (which holds the connected LinkedIn
        // identity/session cookie) must be fetched and merged with our overrides,
        // or every launch fails LinkedIn authentication.
        Map<String, Object> argument = new LinkedHashMap<>(fetchSavedArgument());
        argument.putAll(overrides);

        Map<String, Object> body = Map.of(
                "id", properties.agentId(),
                "argument", argument);

        try {
            LaunchResponse response = webClient.post()
                    .uri("/agents/launch")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(LaunchResponse.class)
                    .block();

            if (response == null || response.containerId() == null) {
                throw new PhantomBusterException("PhantomBuster launch did not return a container id");
            }
            return response.containerId();
        } catch (PhantomBusterException e) {
            throw e;
        } catch (Exception e) {
            throw new PhantomBusterException("Failed to launch PhantomBuster agent", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchSavedArgument() {
        try {
            AgentResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agents/fetch").queryParam("id", properties.agentId()).build())
                    .retrieve()
                    .bodyToMono(AgentResponse.class)
                    .block();

            if (response == null || response.argument() == null) {
                return Map.of();
            }
            return objectMapper.readValue(response.argument(), Map.class);
        } catch (Exception e) {
            throw new PhantomBusterException("Failed to fetch PhantomBuster agent configuration", e);
        }
    }

    /** Polls until the agent's most recent run (matching containerId) has stopped; returns its output log. */
    private String awaitCompletion(String containerId) {
        // Immediately after launch, PhantomBuster can briefly still report the new
        // containerId with isAgentRunning=false (stale, before the container has
        // actually started) - so a bare "matches our id and not running" check can
        // false-positive as "finished" before the run even begins. Require that we
        // have actually observed the run in progress at least once before accepting
        // "not running" as real completion.
        AtomicBoolean seenRunning = new AtomicBoolean(false);

        try {
            AgentRunStatus finalStatus = fetchRunStatus()
                    .flatMap(run -> {
                        if (!containerId.equals(run.containerId())) {
                            return Mono.error(new NotFinishedYet());
                        }
                        if (Boolean.TRUE.equals(run.isAgentRunning())) {
                            seenRunning.set(true);
                            return Mono.error(new NotFinishedYet());
                        }
                        return seenRunning.get() ? Mono.just(run) : Mono.error(new NotFinishedYet());
                    })
                    .retryWhen(Retry.fixedDelay(properties.polling().maxAttempts(),
                                    Duration.ofMillis(properties.polling().intervalMillis()))
                            .filter(NotFinishedYet.class::isInstance))
                    .block();

            if (finalStatus == null) {
                throw new PhantomBusterException("PhantomBuster run did not complete");
            }
            // Re-fetch rather than trusting the snapshot that satisfied the "finished"
            // check above: PhantomBuster can briefly report isAgentRunning=false before
            // the run's final output lines (e.g. a trailing [error] line) are flushed.
            AgentRunStatus settled = fetchRunStatus().block();
            return settled == null ? finalStatus.output() : settled.output();
        } catch (PhantomBusterException e) {
            throw e;
        } catch (Exception e) {
            throw new PhantomBusterException(
                    "Timed out waiting for PhantomBuster agent (containerId=" + containerId + ") to finish", e);
        }
    }

    private Mono<AgentRunStatus> fetchRunStatus() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/agents/fetch-output").queryParam("id", properties.agentId()).build())
                .retrieve()
                .bodyToMono(AgentRunStatus.class);
    }

    /** Surfaces the phantom's own error line (e.g. a LinkedIn auth/CSRF failure) instead of silently returning no data. */
    private void failIfRunErrored(String containerId, String output) {
        if (output == null) {
            return;
        }
        List<String> errorLines = output.lines()
                .filter(line -> line.contains("[error]"))
                .toList();
        if (!errorLines.isEmpty()) {
            throw new PhantomBusterException(
                    "PhantomBuster run (containerId=" + containerId + ") failed: " + String.join(" | ", errorLines));
        }
    }

    private String fetchResultObject(String containerId) {
        try {
            ResultObjectResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/containers/fetch-result-object")
                            .queryParam("id", containerId).build())
                    .retrieve()
                    .bodyToMono(ResultObjectResponse.class)
                    .block();

            return response == null ? null : response.resultObject();
        } catch (Exception e) {
            throw new PhantomBusterException("Failed to fetch PhantomBuster results", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LaunchResponse(String containerId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AgentResponse(String argument) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AgentRunStatus(String containerId, String status, Boolean isAgentRunning, String output) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ResultObjectResponse(String resultObject) {
    }

    private static final class NotFinishedYet extends RuntimeException {
    }
}
