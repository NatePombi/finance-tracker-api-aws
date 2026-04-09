package org.example.financetrackerapi.ai;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.transaction.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@AllArgsConstructor
public class AiController {

    private final AiService aiService;


    @GetMapping("/analyze")
    public ResponseEntity<AiResponse> getAi(@Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        String prompt = aiService.getTransactionsForAiAnalysis(email);
        String aiResponse = aiService.analyzeSpending(prompt);
        return ResponseEntity.ok(new AiResponse(aiResponse));
    }
}
