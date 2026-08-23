package br.edu.cpa.cpainteligente.dto.response;

import br.edu.cpa.cpainteligente.entity.Questionnaire;
import br.edu.cpa.cpainteligente.entity.Question;
import br.edu.cpa.cpainteligente.entity.enums.SinaesAxis;

import java.time.Instant;
import java.util.List;

public record QuestionnaireResponse(
        String id,
        String title,
        String description,
        boolean active,
        String periodLabel,
        List<QuestionItem> questions,
        Instant createdAt
) {
    public record QuestionItem(
            String id,
            String text,
            SinaesAxis axis,
            Integer orderNum
    ) {}

    public static QuestionnaireResponse from(Questionnaire q) {
        List<QuestionItem> items = q.getQuestions().stream()
                .map(question -> new QuestionItem(
                        question.getId(),
                        question.getText(),
                        question.getAxis(),
                        question.getOrderNum()
                ))
                .toList();

        return new QuestionnaireResponse(
                q.getId(),
                q.getTitle(),
                q.getDescription(),
                q.isActive(),
                q.getPeriodLabel(),
                items,
                q.getCreatedAt()
        );
    }
}