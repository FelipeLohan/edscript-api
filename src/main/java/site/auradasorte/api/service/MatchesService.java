package site.auradasorte.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Service
public class MatchesService {

    private final RestClient betClient;
    private final RestClient analysisClient;
    private final ObjectMapper objectMapper;

    @Value("${bet.token}")
    private String token;

    public MatchesService(
            @Value("${bet.base-url}") String betBaseUrl,
            @Value("${analysis.base-url}") String analysisBaseUrl,
            ObjectMapper objectMapper) {
        this.betClient = RestClient.builder()
                .baseUrl(betBaseUrl)
                .build();
        this.analysisClient = RestClient.builder()
                .baseUrl(analysisBaseUrl)
                .build();
        this.objectMapper = objectMapper;
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

    public Object analyzeMatch(String matchId) {
        Object matchData = betClient.get()
                .uri("/v3/event/view?token={token}&event_id={matchId}", token, matchId)
                .retrieve()
                .body(Object.class);

        return analysisClient.post()
                .uri("/analyze")
                .body(matchData)
                .retrieve()
                .body(Object.class);
    }
}
