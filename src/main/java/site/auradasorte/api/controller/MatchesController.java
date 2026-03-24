package site.auradasorte.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.auradasorte.api.service.MatchesService;

@RestController
@RequestMapping("/matches")
public class MatchesController {

    private final MatchesService matchesService;

    public MatchesController(MatchesService matchesService) {
        this.matchesService = matchesService;
    }

    @GetMapping("/inplay")
    public ResponseEntity<Object> getInplayMatches(@RequestParam(name = "sport_id", defaultValue = "1") int sportId) {
        return ResponseEntity.ok(matchesService.getInplayMatches(sportId));
    }

    @GetMapping("/analyze")
    public ResponseEntity<Object> analyzeMatch(@RequestParam(name = "match_id") String matchId) {
        return ResponseEntity.ok(matchesService.analyzeMatch(matchId));
    }
}
