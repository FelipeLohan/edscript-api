package site.auradasorte.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MatchesService {

    private final RestClient betClient;
    private final RestClient analysisClient;

    @Value("${bet.token}")
    private String token;

    public MatchesService(
            @Value("${bet.base-url}") String betBaseUrl,
            @Value("${analysis.base-url}") String analysisBaseUrl) {
        this.betClient = RestClient.builder()
                .baseUrl(betBaseUrl)
                .build();
        this.analysisClient = RestClient.builder()
                .baseUrl(analysisBaseUrl)
                .build();
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
