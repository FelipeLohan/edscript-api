package site.auradasorte.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

@Service
public class MatchesService {

    private final RestClient betClient;
    private final RestClient analysisClient;
    private final ObjectMapper objectMapper;
    private final int analysisMaxAttempts;
    private final long analysisBackoffMs;

    @Value("${bet.token}")
    private String token;

    public MatchesService(
            @Value("${bet.base-url}") String betBaseUrl,
            @Value("${analysis.base-url}") String analysisBaseUrl,
            @Value("${integration.http.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${integration.http.read-timeout-ms:8000}") int readTimeoutMs,
            @Value("${integration.analysis.max-attempts:3}") int analysisMaxAttempts,
            @Value("${integration.analysis.backoff-ms:300}") long analysisBackoffMs,
            ObjectMapper objectMapper) {
        this.betClient = buildClient(betBaseUrl, connectTimeoutMs, readTimeoutMs);
        this.analysisClient = buildClient(analysisBaseUrl, connectTimeoutMs, readTimeoutMs);
        this.analysisMaxAttempts = Math.max(1, analysisMaxAttempts);
        this.analysisBackoffMs = Math.max(0, analysisBackoffMs);
        this.objectMapper = objectMapper;
    }

        private RestClient buildClient(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(baseUrl)
            .build();
        }

    public JsonNode getMatches() {
        try {
            var resource = new ClassPathResource("mock/matches.json");
            return objectMapper.readTree(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mock matches data", e);
        }
    }

    public JsonNode getMatchById(String matchId) {
        for (JsonNode item : getMatches()) {
            JsonNode match = item.get("match");
            if (match != null && matchId.equals(match.get("id").asText())) {
                return item;
            }
        }
        return null;
    }

    public Object getInplayMatches(int sportId) {
        return betClient.get()
                .uri("/v3/events/inplay?sport_id={sportId}&token={token}", sportId, token)
                .retrieve()
                .body(Object.class);
    }

    public Object getUpcomingMatches(int sportId, String leagueId) {
        return betClient.get()
                .uri("/v3/events/upcoming?sport_id={sportId}&token={token}&league_id={leagueId}", sportId, token, leagueId)
                .retrieve()
                .body(Object.class);
    }

    public Object getPlayEventView(String eventId) {
        return betClient.get()
                .uri("/v1/event/view?token={token}&event_id={eventId}", token, eventId)
                .retrieve()
                .body(Object.class);
    }

    public Object analyzeMatch(String matchId) {
        try {
            Object matchData = getPlayEventView(matchId);
            return postAnalysisWithRetry(matchId, matchData);
        } catch (Exception e) {
            return buildAnalysisError(matchId, e);
        }
    }

    public Object getMatchAnalysis(String matchId) {
        try {
            Object matchData = getPlayEventView(matchId);
            return postAnalysisWithRetry(matchId, matchData);
        } catch (Exception e) {
            return buildAnalysisError(matchId, e);
        }
    }

    private Object postAnalysisWithRetry(String matchId, Object matchData) {
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= analysisMaxAttempts; attempt++) {
            try {
                return analysisClient.post()
                        .uri("/matches/analyze/{matchId}", matchId)
                        .body(matchData)
                        .retrieve()
                        .body(Object.class);
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == analysisMaxAttempts) {
                    break;
                }
                if (analysisBackoffMs > 0) {
                    try {
                        Thread.sleep(analysisBackoffMs);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrompido durante chamada de analise.", interrupted);
                    }
                }
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new RuntimeException("Falha desconhecida ao consultar servico de analise.");
    }

    private Map<String, Object> buildAnalysisError(String matchId, Exception e) {
        String details = e.getMessage() == null ? "Erro sem detalhes." : e.getMessage();
        return Map.of(
                "status", "error",
                "code", "ANALYSIS_UNAVAILABLE",
                "message", "Nao foi possivel consultar o servico de analise no momento.",
                "match_id", matchId,
                "details", details
        );
    }

    public Object getMatchDashboard(String matchId) {
        Object matchData = getPlayEventView(matchId);

        return analysisClient.post()
                .uri("/matches/dashboard/{match_id}", matchId)
                .body(matchData)
                .retrieve()
                .body(Object.class);
    }
}
