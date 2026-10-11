package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.repository.AnswerRepository;
import br.edu.cpa.cpainteligente.security.AuthUtils;
import br.edu.cpa.cpainteligente.service.BulkSentimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sentiment")
@RequiredArgsConstructor
public class SentimentController {

    private final BulkSentimentService bulkSentimentService;
    private final AnswerRepository answerRepository;

    // Analisa uma única resposta — síncrono, útil para testar antes do lote completo
    @PostMapping("/analyze/{answerId}")
    public ResponseEntity<Void> analyzeOne(@PathVariable String answerId) {
        bulkSentimentService.analyzeAndSaveOne(answerId);
        return ResponseEntity.ok().build();
    }

    // Dispara análise de TODOS os comentários pendentes em background
    @PostMapping("/analyze-all")
    public ResponseEntity<Map<String, Object>> analyzeAll() {
        String institutionId = AuthUtils.getInstitutionId();
        long pending = answerRepository
                .countByCommentIsNotNullAndSentimentIsNullAndResponse_Institution_Id(institutionId);

        bulkSentimentService.analyzeAllPending(institutionId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "message", "Análise de sentimento iniciada em segundo plano.",
                "pending", pending
        ));
    }

    // Consulta o progresso — chame isso de tempos em tempos enquanto o lote roda
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        String institutionId = AuthUtils.getInstitutionId();

        long total = answerRepository.countByCommentIsNotNullAndResponse_Institution_Id(institutionId);
        long analyzed = answerRepository
                .countByCommentIsNotNullAndSentimentIsNotNullAndResponse_Institution_Id(institutionId);

        return ResponseEntity.ok(Map.of(
                "totalComments", total,
                "analyzed", analyzed,
                "pending", total - analyzed
        ));
    }
}