package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.entity.Answer;
import br.edu.cpa.cpainteligente.repository.AnswerRepository;
import br.edu.cpa.cpainteligente.service.SentimentService.SentimentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkSentimentService {

    private final AnswerRepository answerRepository;
    private final SentimentService sentimentService;

    // Roda em thread separada — a requisição HTTP que chamou isso já retornou há tempos
    @Async("sentimentExecutor")
    public void analyzeAllPending(String institutionId) {
        List<Answer> pending = answerRepository
                .findByCommentIsNotNullAndSentimentIsNullAndResponse_Institution_Id(institutionId);

        log.info("Iniciando análise de sentimento em lote: {} comentário(s) pendente(s).", pending.size());

        int ok = 0, fail = 0;

        for (Answer answer : pending) {
            try {
                analyzeAndSaveOne(answer.getId());
                ok++;
            } catch (Exception e) {
                fail++;
                log.error("Falha ao analisar answer {}: {}", answer.getId(), e.getMessage());
            }

            if ((ok + fail) % 25 == 0) {
                log.info("Progresso: {}/{} analisados", ok + fail, pending.size());
            }
        }

        log.info("Análise em lote concluída: {} ok, {} falhas, de {} total.", ok, fail, pending.size());
    }

    @Transactional
    public void analyzeAndSaveOne(String answerId) {
        Answer answer = answerRepository.findByIdWithQuestion(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer não encontrada: " + answerId));

        if (answer.getComment() == null || answer.getComment().isBlank()) {
            return;
        }

        SentimentResult result = sentimentService.analyze(
                answer.getComment(),
                answer.getQuestion().getAxis().getLabel()
        );

        answer.setSentiment(result.sentiment());
        answer.setSentimentScore(result.score());
        answer.setSentimentReason(result.reason());
        answer.setAnalyzedAt(Instant.now());

        answerRepository.save(answer);
    }

    // Versão assíncrona para disparar a partir do fluxo do bot (WhatsApp), sem travar a resposta ao aluno
    @Async("sentimentExecutor")
    public void analyzeAndSaveOneAsync(String answerId) {
        try {
            analyzeAndSaveOne(answerId);
        } catch (Exception e) {
            log.error("Falha ao analisar sentimento em background (answerId={}): {}", answerId, e.getMessage());
        }
    }
}