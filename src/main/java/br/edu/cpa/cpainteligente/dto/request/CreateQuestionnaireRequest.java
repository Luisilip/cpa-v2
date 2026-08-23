package br.edu.cpa.cpainteligente.dto.request;

import br.edu.cpa.cpainteligente.entity.enums.SinaesAxis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuestionnaireRequest(

        @NotBlank(message = "Título é obrigatório")
        String title,

        String description,
        String periodLabel,

        // Se não informado, usa as perguntas padrão SINAES
        List<QuestionRequest> questions
) {
    // Record interno — só usado dentro deste DTO
    public record QuestionRequest(
            @NotBlank String text,
            @NotNull SinaesAxis axis
    ) {}
}
