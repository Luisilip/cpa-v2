package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.entity.enums.Sentiment;
import br.edu.cpa.cpainteligente.whatsapp.CpaAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentService {

    private final CpaAgent cpaAgent;

    // Parsing tolerante — não depende do modelo acertar JSON perfeito
    private static final Pattern SENTIMENT_PATTERN =
            Pattern.compile("SENTIMENT:\\s*(POSITIVE|NEUTRAL|NEGATIVE)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCORE_PATTERN =
            Pattern.compile("SCORE:\\s*(-?\\d+(?:[.,]\\d+)?)");
    private static final Pattern REASON_PATTERN =
            Pattern.compile("REASON:\\s*(.+)");

    public record SentimentResult(Sentiment sentiment, Double score, String reason) {}

    public SentimentResult analyze(String comment, String axisLabel) {
        if (comment == null || comment.isBlank()) {
            return fallback();
        }

        String prompt = String.format("""
            Eixo avaliado: %s
            Comentário do estudante: "%s"

            Classifique o sentimento deste comentário.
            """, axisLabel, comment);

        try {
            String raw = cpaAgent.analyzeSentiment(prompt);
            return parse(raw);
        } catch (Exception e) {
            log.warn("Falha ao analisar sentimento via Ollama: {}", e.getMessage());
            return fallback();
        }
    }

    private SentimentResult parse(String raw) {
        Sentiment sentiment = Sentiment.NEUTRAL;
        Double score = 0.0;
        String reason = "Não foi possível determinar o motivo automaticamente.";

        Matcher sm = SENTIMENT_PATTERN.matcher(raw);
        if (sm.find()) {
            sentiment = Sentiment.valueOf(sm.group(1).toUpperCase());
        }

        Matcher scm = SCORE_PATTERN.matcher(raw);
        if (scm.find()) {
            try {
                score = Double.parseDouble(scm.group(1).replace(",", "."));
                score = Math.max(-1.0, Math.min(1.0, score)); // clamp de segurança
            } catch (NumberFormatException ignored) {}
        }

        Matcher rm = REASON_PATTERN.matcher(raw);
        if (rm.find()) {
            reason = rm.group(1).trim();
        }

        return new SentimentResult(sentiment, score, reason);
    }

    private SentimentResult fallback() {
        return new SentimentResult(Sentiment.NEUTRAL, 0.0, "Análise automática indisponível.");
    }
}