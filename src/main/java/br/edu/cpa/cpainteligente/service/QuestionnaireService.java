package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.dto.request.CreateQuestionnaireRequest;
import br.edu.cpa.cpainteligente.dto.response.QuestionnaireResponse;
import br.edu.cpa.cpainteligente.entity.Institution;
import br.edu.cpa.cpainteligente.entity.Question;
import br.edu.cpa.cpainteligente.entity.Questionnaire;
import br.edu.cpa.cpainteligente.exception.ApiException;
import br.edu.cpa.cpainteligente.repository.InstitutionRepository;
import br.edu.cpa.cpainteligente.repository.QuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final InstitutionRepository institutionRepository;

    // Perguntas padrão SINAES — usadas quando não informadas no request
    private static final List<CreateQuestionnaireRequest.QuestionRequest> DEFAULT_QUESTIONS = List.of(
            new CreateQuestionnaireRequest.QuestionRequest(
                    "Como você avalia a qualidade do ensino oferecido?",
                    br.edu.cpa.cpainteligente.entity.enums.SinaesAxis.ENSINO),
            new CreateQuestionnaireRequest.QuestionRequest(
                    "Como você avalia as atividades de pesquisa da instituição?",
                    br.edu.cpa.cpainteligente.entity.enums.SinaesAxis.PESQUISA),
            new CreateQuestionnaireRequest.QuestionRequest(
                    "Como você avalia os projetos de extensão oferecidos?",
                    br.edu.cpa.cpainteligente.entity.enums.SinaesAxis.EXTENSAO),
            new CreateQuestionnaireRequest.QuestionRequest(
                    "Como você avalia a gestão e administração da instituição?",
                    br.edu.cpa.cpainteligente.entity.enums.SinaesAxis.GESTAO),
            new CreateQuestionnaireRequest.QuestionRequest(
                    "Como você avalia a infraestrutura e instalações físicas?",
                    br.edu.cpa.cpainteligente.entity.enums.SinaesAxis.INFRAESTRUTURA)
    );

    public List<QuestionnaireResponse> listAll(String institutionId) {
        return questionnaireRepository.findByInstitutionId(institutionId)
                .stream()
                .map(QuestionnaireResponse::from)
                .toList();
    }

    @Transactional
    public QuestionnaireResponse create(CreateQuestionnaireRequest request, String institutionId) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> ApiException.notFound("Instituição não encontrada"));

        // Usa perguntas do request ou as padrão SINAES
        List<CreateQuestionnaireRequest.QuestionRequest> questionRequests =
                (request.questions() != null && !request.questions().isEmpty())
                        ? request.questions()
                        : DEFAULT_QUESTIONS;

        // AtomicInteger para incrementar o order dentro de uma lambda
        // (variáveis usadas em lambdas precisam ser efetivamente finais em Java)
        AtomicInteger order = new AtomicInteger(1);

        Questionnaire questionnaire = Questionnaire.builder()
                .title(request.title())
                .description(request.description())
                .periodLabel(request.periodLabel())
                .institution(institution)
                .build();

        List<Question> questions = questionRequests.stream()
                .map(qr -> Question.builder()
                        .text(qr.text())
                        .axis(qr.axis())
                        .orderNum(order.getAndIncrement())
                        .questionnaire(questionnaire)
                        .build())
                .toList();

        questionnaire.setQuestions(questions);

        return QuestionnaireResponse.from(questionnaireRepository.save(questionnaire));
    }

    @Transactional
    public QuestionnaireResponse toggleActive(String id, String institutionId) {
        Questionnaire questionnaire = questionnaireRepository
                .findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> ApiException.notFound("Questionário não encontrado"));

        questionnaire.setActive(!questionnaire.isActive());
        return QuestionnaireResponse.from(questionnaireRepository.save(questionnaire));
    }
}