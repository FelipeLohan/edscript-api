package site.auradasorte.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import site.auradasorte.api.service.MatchesService;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchesController.class)
class MatchesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchesService matchesService;

    @Test
    void analyzeMatchShouldReturnOkWhenServiceSucceeds() throws Exception {
        when(matchesService.analyzeMatch("123"))
                .thenReturn(Map.of("status", "success", "match_id", 123));

        mockMvc.perform(get("/matches/analyze")
                        .param("match_id", "123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void analyzeMatchShouldReturnBadGatewayWhenServiceReturnsErrorContract() throws Exception {
        when(matchesService.analyzeMatch("999"))
                .thenReturn(Map.of(
                        "status", "error",
                        "code", "ANALYSIS_UNAVAILABLE",
                        "message", "Nao foi possivel consultar o servico de analise no momento.",
                        "match_id", "999"
                ));

        mockMvc.perform(get("/matches/analyze")
                        .param("match_id", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.code").value("ANALYSIS_UNAVAILABLE"));
    }
}
