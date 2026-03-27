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
@RequestMapping("/cards")
@Tag(name = "Cards", description = "Endpoints para análise de risco de cartões (Modelos Preditivos)")
public class CardsController {

    private final MatchesService matchesService;

    public CardsController(MatchesService matchesService) {
        this.matchesService = matchesService;
    }

    @Operation(summary = "Análise de risco de cartões por Time", description = "Retorna a média de probabilidade e o top N jogadores do time com maior chance de tomar cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise do time retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Time não encontrado"),
        @ApiResponse(responseCode = "502", description = "Erro na comunicação com a API de IA")
    })
    @GetMapping("/team/{teamName}")
    public ResponseEntity<Object> getTeamCardsAnalysis(
            @Parameter(description = "Nome do time", required = true) @PathVariable String teamName,
            @Parameter(description = "Quantidade de jogadores no top list") @RequestParam(name = "top_n", defaultValue = "10") int topN) {
        Object response = matchesService.getTeamCardsAnalysis(teamName, topN);
        if (response instanceof Map<?, ?> responseMap && "error".equals(responseMap.get("status"))) {
            return ResponseEntity.status(502).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Análise de risco de cartões por Jogador", description = "Retorna o perfil completo e predição de risco de cartões para um jogador específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Análise do jogador retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "502", description = "Erro na comunicação com a API de IA")
    })
    @GetMapping("/player/{playerName}")
    public ResponseEntity<Object> getPlayerCardsAnalysis(
            @Parameter(description = "Nome do jogador", required = true) @PathVariable String playerName) {
        Object response = matchesService.getPlayerCardsAnalysis(playerName);
        if (response instanceof Map<?, ?> responseMap && "error".equals(responseMap.get("status"))) {
            return ResponseEntity.status(502).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
