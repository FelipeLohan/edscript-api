package site.auradasorte.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.auradasorte.api.service.MatchesService;

@RestController
@RequestMapping("/matches")
@Tag(name = "Matches", description = "Endpoints para consulta de partidas")
public class MatchesController {

    private final MatchesService matchesService;

    public MatchesController(MatchesService matchesService) {
        this.matchesService = matchesService;
    }

    @Operation(summary = "Listar partidas análisadas Mockadas", description = "Retorna a lista de partidas mockadas")
    @ApiResponse(responseCode = "200", description = "Lista de partidas retornada com sucesso")
    @GetMapping
    public ResponseEntity<Object> getMatches() {
        return ResponseEntity.ok(matchesService.getMatches());
    }

    @Operation(summary = "Buscar partida análisada Mockada por ID", description = "Retorna os dados de uma partida específica pelo seu ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Partida encontrada"),
        @ApiResponse(responseCode = "404", description = "Partida não encontrada")
    })
    @GetMapping("/{matchId}")
    public ResponseEntity<Object> getMatchById(
            @Parameter(description = "ID da partida", required = true) @PathVariable String matchId) {
        var match = matchesService.getMatchById(matchId);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @Operation(summary = "Todas as partidas ao vivo", description = "Retorna as partidas em andamento de um determinado esporte via B365 API")
    @ApiResponse(responseCode = "200", description = "Partidas ao vivo retornadas com sucesso")
    @GetMapping("/inplay")
    public ResponseEntity<Object> getInplayMatches(
            @Parameter(description = "ID do esporte (1 = futebol)") @RequestParam(name = "sport_id", defaultValue = "1") int sportId) {
        return ResponseEntity.ok(matchesService.getInplayMatches(sportId));
    }

    @Operation(summary = "Todas as próximas partidas", description = "Retorna as próximas partidas de uma liga. Por padrão retorna a Série A do Brasileirão (league_id=155)")
    @ApiResponse(responseCode = "200", description = "Próximas partidas retornadas com sucesso")
    @GetMapping("/upcoming")
    public ResponseEntity<Object> getUpcomingMatches(
            @Parameter(description = "ID do esporte (1 = futebol)") @RequestParam(name = "sport_id", defaultValue = "1") int sportId,
            @Parameter(description = "ID da liga (155 = Série A Brasileirão)") @RequestParam(name = "league_id", defaultValue = "155") String leagueId) {
        return ResponseEntity.ok(matchesService.getUpcomingMatches(sportId, leagueId));
    }

    @Operation(summary = "Detalhes de partida por id", description = "Retorna os detalhes de um evento específico via B365 API (v1/event/view)")
    @ApiResponse(responseCode = "200", description = "Detalhes do evento retornados com sucesso")
    @GetMapping("/play/{id}")
    public ResponseEntity<Object> getPlayEventView(
            @Parameter(description = "ID do evento na B365", required = true) @PathVariable String id) {
        return ResponseEntity.ok(matchesService.getPlayEventView(id));
    }

    @Operation(summary = "Analisar partida usando o serviço de IA", description = "Busca os dados da partida na B365 e envia para o serviço de análise")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "match_id não informado")
    })
    @GetMapping("/analyze")
    public ResponseEntity<Object> analyzeMatch(
            @Parameter(description = "ID do evento na B365", required = true) @RequestParam(name = "match_id") String matchId) {
        Object response = matchesService.analyzeMatch(matchId);
        if (response instanceof Map<?, ?> responseMap && "error".equals(responseMap.get("status"))) {
            return ResponseEntity.status(502).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar análise de partida por ID", description = "Retorna a análise de uma partida diretamente do serviço de dados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Análise não encontrada")
    })
    @GetMapping("/analyze/{matchId}")
    public ResponseEntity<Object> getMatchAnalysis(
            @Parameter(description = "ID da partida no serviço de análise", required = true) @PathVariable String matchId) {
        Object response = matchesService.getMatchAnalysis(matchId);
        if (response instanceof Map<?, ?> responseMap && "error".equals(responseMap.get("status"))) {
            return ResponseEntity.status(502).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
