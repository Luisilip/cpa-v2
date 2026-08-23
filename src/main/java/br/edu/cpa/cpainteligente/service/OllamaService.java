package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.entity.Question;
import br.edu.cpa.cpainteligente.whatsapp.CpaAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final CpaAgent cpaAgent;

    // Gera a saudação inicial personalizada
    public String generateWelcome(String studentName, String questionnaireTitle) {
        String prompt = String.format("""
            Gere uma saudação inicial curta (máximo 3 linhas) para %s que vai participar
            da avaliação CPA "%s". Explique que vai avaliar 5 eixos com notas de 1 a 5.
            Seja animado e use 1 emoji.
            """, studentName, questionnaireTitle);

        return callWithFallback(prompt,
                String.format("Olá, %s! 👋 Vamos iniciar a avaliação CPA — %s. " +
                                "Você vai avaliar 5 aspectos com notas de 1 (péssimo) a 5 (ótimo).",
                        studentName, questionnaireTitle));
    }

    // Gera o texto da pergunta com contexto
    public String generateQuestion(Question question, int current, int total,
                                   String progressBar) {
        String prompt = String.format("""
            Formule de maneira amigável e breve a seguinte pergunta de avaliação:
            Eixo: %s (%d de %d)
            Pergunta: %s
            Progresso: %s
            Peça uma nota de 1 a 5 e um comentário opcional. Máximo 4 linhas.
            """,
                question.getAxis().getLabel(), current, total,
                question.getText(), progressBar);

        return callWithFallback(prompt,
                String.format("%s\n\n*Pergunta %d/%d — %s*\n%s\n\n_(1=Péssimo | 5=Ótimo)_",
                        progressBar, current, total,
                        question.getAxis().getLabel(), question.getText()));
    }

    // Gera confirmação após receber uma nota
    public String generateConfirmation(String axisLabel, int rating, boolean hasComment) {
        String ratingWord = switch (rating) {
            case 1 -> "Péssimo";
            case 2 -> "Ruim";
            case 3 -> "Regular";
            case 4 -> "Bom";
            case 5 -> "Ótimo";
            default -> String.valueOf(rating);
        };

        String prompt = String.format("""
            O aluno avaliou o eixo %s com nota %d (%s)%s.
            Gere uma confirmação breve e positiva (1 linha, 1 emoji).
            """,
                axisLabel, rating, ratingWord,
                hasComment ? " e deixou um comentário" : "");

        return callWithFallback(prompt,
                String.format("✅ Nota %d registrada para %s!", rating, axisLabel));
    }

    // Gera mensagem de conclusão personalizada
    public String generateCompletion(String studentFirstName) {
        String prompt = String.format("""
            A avaliação CPA de %s foi concluída com sucesso (todos os 5 eixos avaliados).
            Gere uma mensagem de agradecimento calorosa e breve (máximo 3 linhas, 1 emoji).
            Mencione que as respostas são confidenciais.
            """, studentFirstName);

        return callWithFallback(prompt,
                String.format("✅ Avaliação concluída, %s! Muito obrigado pela participação. " +
                                "Suas respostas são confidenciais e vão ajudar a melhorar nossa instituição! 🎓",
                        studentFirstName));
    }

    // Gera mensagem de pausa
    public String generatePauseMessage() {
        return callWithFallback(
                "Gere uma mensagem curta confirmando que a avaliação foi pausada e " +
                        "que o aluno pode continuar quando quiser. 1 emoji, máximo 2 linhas.",
                "⏸️ Avaliação pausada! Pode continuar quando quiser, " +
                        "é só me mandar uma mensagem.");
    }

    // Interpreta se a mensagem contém uma nota válida (1-5)
    // Retorna a nota ou -1 se não identificou
    public int extractRating(String message, String axisContext) {
        // Primeiro tenta extração direta sem chamar o LLM (mais rápido e confiável)
        String cleaned = message.trim().replaceAll("[^0-9]", "");
        if (cleaned.length() == 1) {
            int value = Integer.parseInt(cleaned);
            if (value >= 1 && value <= 5) return value;
        }

        // Se não conseguiu extrair diretamente, pede ajuda ao LLM
        String prompt = String.format("""
            O aluno está avaliando o eixo %s e enviou a seguinte mensagem: "%s"
            Identifique se há uma nota de 1 a 5 nesta mensagem.
            Responda APENAS com o número (1, 2, 3, 4 ou 5) ou com "NENHUMA" se não houver nota.
            Não escreva mais nada.
            """, axisContext, message);

        try {
            String response = cpaAgent.generate(prompt).trim();
            if (response.matches("[1-5]")) {
                return Integer.parseInt(response);
            }
        } catch (Exception e) {
            log.warn("Ollama não disponível para extração de nota: {}", e.getMessage());
        }

        return -1; // Sem nota identificada
    }

    // Chama o Ollama com fallback para texto padrão se falhar
    private String callWithFallback(String prompt, String fallback) {
        try {
            return cpaAgent.generate(prompt);
        } catch (Exception e) {
            log.warn("Ollama indisponível, usando texto padrão: {}", e.getMessage());
            return fallback;
        }
    }
}