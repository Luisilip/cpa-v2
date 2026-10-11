package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.dto.response.BotFlowResult;
import br.edu.cpa.cpainteligente.entity.*;
import br.edu.cpa.cpainteligente.entity.enums.SessionStatus;
import br.edu.cpa.cpainteligente.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotFlowService {

    private final BotSessionRepository    sessionRepository;
    private final StudentRepository       studentRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final ResponseRepository      responseRepository;
    private final AnswerRepository        answerRepository;
    private final OllamaService           ollamaService;
    private final BulkSentimentService bulkSentimentService;

    private static final int SESSION_TIMEOUT_HOURS = 24;
    private static final List<String> PAUSE_KEYWORDS = List.of(
            "pausar", "pausa", "depois", "agora não", "agora nao",
            "mais tarde", "responder depois", "volto depois"
    );

    @Transactional
    public BotFlowResult handleIncomingMessage(
            String institutionId,
            String phoneHash,
            String phoneNumber,
            String messageText
    ) {
        // 1. Sessão ativa?
        Optional<BotSession> activeSession = sessionRepository
                .findByPhoneHashAndStatusIn(
                        phoneHash,
                        List.of(SessionStatus.IN_PROGRESS, SessionStatus.PAUSED)
                );

        if (activeSession.isPresent()) {
            BotSession session = activeSession.get();

            if (session.getExpiresAt().isBefore(Instant.now())) {
                session.setStatus(SessionStatus.EXPIRED);
                sessionRepository.save(session);
                return BotFlowResult.of(
                        "Sua sessão expirou por inatividade 😔\n" +
                                "Entre em contato com a coordenação para reiniciar a avaliação."
                );
            }

            return processMessage(session, messageText);
        }

        // 2. Aluno cadastrado?
        Optional<Student> student = studentRepository
                .findByPhoneHashAndInstitutionId(phoneHash, institutionId);

        if (student.isEmpty()) {
            return BotFlowResult.of(
                    "Olá! 👋 Seu número não está cadastrado no sistema CPA.\n" +
                            "Verifique com a coordenação da sua instituição."
            );
        }

        return startNewSession(student.get(), institutionId);
    }

    private BotFlowResult startNewSession(Student student, String institutionId) {
        List<Questionnaire> ativos = questionnaireRepository
                .findByInstitutionId(institutionId)
                .stream()
                .filter(Questionnaire::isActive)
                .toList();

        if (ativos.isEmpty()) {
            return BotFlowResult.of(
                    "Olá, " + student.getName() + "! 👋\n" +
                            "Não há avaliações ativas no momento."
            );
        }

        Questionnaire questionnaire = questionnaireRepository
                .findActiveWithQuestions(ativos.get(0).getId(), institutionId)
                .orElse(ativos.get(0));

        if (responseRepository.findByStudentIdAndQuestionnaireId(
                student.getId(), questionnaire.getId()).isPresent()) {
            return BotFlowResult.of(
                    "Você já participou desta avaliação. Obrigado! 🎓"
            );
        }

        Response response = responseRepository.save(
                Response.builder()
                        .institution(student.getInstitution())
                        .student(student)
                        .questionnaire(questionnaire)
                        .build()
        );

        BotSession session = sessionRepository.save(
                BotSession.builder()
                        .phoneHash(student.getPhoneHash())
                        .institution(student.getInstitution())
                        .student(student)
                        .questionnaire(questionnaire)
                        .response(response)
                        .currentQuestionOrder(1)
                        .status(SessionStatus.IN_PROGRESS)
                        .expiresAt(Instant.now().plus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS))
                        .build()
        );

        Question firstQuestion = questionnaire.getQuestions().get(0);
        int total = questionnaire.getQuestions().size();

        String welcome = ollamaService.generateWelcome(
                student.getName(), questionnaire.getTitle());

        String firstQuestionText = ollamaService.generateQuestion(
                firstQuestion, 1, total, buildProgressBar(0, total));

        return BotFlowResult.of(welcome + "\n\n" + firstQuestionText);
    }

    @Transactional
    private BotFlowResult processMessage(BotSession session, String messageText) {
        // Retomada de pausa
        if (session.getStatus() == SessionStatus.PAUSED) {
            session.setStatus(SessionStatus.IN_PROGRESS);
            session.setLastMessageAt(Instant.now());
            session.setExpiresAt(Instant.now().plus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS));
            sessionRepository.save(session);

            Questionnaire q = loadWithQuestions(session);
            Question current = findQuestion(q.getQuestions(), session.getCurrentQuestionOrder());
            int total = q.getQuestions().size();
            int answered = (int) answerRepository.countByResponseId(session.getResponse().getId());

            return BotFlowResult.of(
                    "Bem-vindo(a) de volta! 😊\n\n" +
                            ollamaService.generateQuestion(current, session.getCurrentQuestionOrder(),
                                    total, buildProgressBar(answered, total))
            );
        }

        // Pedido de pausa
        if (isPauseRequest(messageText)) {
            session.setStatus(SessionStatus.PAUSED);
            session.setLastMessageAt(Instant.now());
            sessionRepository.save(session);
            return BotFlowResult.of(ollamaService.generatePauseMessage());
        }

        // Carrega dados
        Questionnaire q = loadWithQuestions(session);
        List<Question> questions = q.getQuestions();
        int total = questions.size();
        Question currentQuestion = findQuestion(questions, session.getCurrentQuestionOrder());

        if (currentQuestion == null) {
            return BotFlowResult.of("Ocorreu um erro. Por favor, contate a coordenação.");
        }

        // Tenta extrair nota
        int rating = ollamaService.extractRating(
                messageText, currentQuestion.getAxis().getLabel());

        if (rating == -1) {
            // Sem nota — pode ser um comentário livre para a pergunta anterior
            // Salva como comentário na última resposta se existir
            Optional<Answer> lastAnswer = answerRepository
                    .findByResponseIdAndQuestionId(
                            session.getResponse().getId(),
                            currentQuestion.getId()
                    );

            if (lastAnswer.isPresent() && lastAnswer.get().getComment() == null) {
                lastAnswer.get().setComment(messageText);
                answerRepository.save(lastAnswer.get());
                return BotFlowResult.of(
                        "💬 Comentário registrado! " + getNextQuestionOrFinish(session, questions, total)
                );
            }

            return BotFlowResult.of(
                    "Não entendi. 🤔 Por favor, envie uma nota de *1 a 5* para avaliar " +
                            currentQuestion.getAxis().getLabel() + ".\n" +
                            "_(1=Péssimo | 2=Ruim | 3=Regular | 4=Bom | 5=Ótimo)_"
            );
        }

        // Salva a resposta
        Answer answer = answerRepository
                .findByResponseIdAndQuestionId(
                        session.getResponse().getId(), currentQuestion.getId())
                .orElse(Answer.builder()
                        .response(session.getResponse())
                        .question(currentQuestion)
                        .build());

        answer.setRating(rating);

        String possibleComment = messageText
                .replaceAll("(?i)(nota\\s*)?[1-5][.)]?\\s*", "")  // remove "3.", "nota 3", etc.
                .trim();
        if (!possibleComment.isBlank()) {
            answer.setComment(possibleComment);
        }

        answerRepository.save(answer);

        if (answer.getComment() != null && !answer.getComment().isBlank()) {
            bulkSentimentService.analyzeAndSaveOneAsync(answer.getId());
        }

        log.info("Answer salva — eixo: {} | nota: {} | sessão: {}",
                currentQuestion.getAxis(), rating, session.getId());

        int answered = (int) answerRepository.countByResponseId(session.getResponse().getId());
        String confirmation = ollamaService.generateConfirmation(
                currentQuestion.getAxis().getLabel(), rating, false);

        // Próxima pergunta ou conclusão
        int nextOrder = session.getCurrentQuestionOrder() + 1;
        Question nextQuestion = findQuestion(questions, nextOrder);

        if (nextQuestion == null) {
            // Concluído!
            session.setStatus(SessionStatus.COMPLETED);
            session.setCompletedAt(Instant.now());
            session.setLastMessageAt(Instant.now());
            sessionRepository.save(session);

            String firstName = session.getStudent().getName().split(" ")[0];
            return BotFlowResult.of(
                    confirmation + "\n\n" +
                            buildProgressBar(total, total) + "\n\n" +
                            ollamaService.generateCompletion(firstName)
            );
        }

        // Avança sessão
        session.setCurrentQuestionOrder(nextOrder);
        session.setLastMessageAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS));
        sessionRepository.save(session);

        String nextQuestionText = ollamaService.generateQuestion(
                nextQuestion, nextOrder, total, buildProgressBar(answered, total));

        return BotFlowResult.of(confirmation + "\n\n" + nextQuestionText);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String getNextQuestionOrFinish(BotSession session,
                                           List<Question> questions, int total) {
        int nextOrder = session.getCurrentQuestionOrder() + 1;
        Question next = findQuestion(questions, nextOrder);
        if (next == null) return "Avaliação concluída!";
        int answered = (int) answerRepository.countByResponseId(session.getResponse().getId());
        session.setCurrentQuestionOrder(nextOrder);
        sessionRepository.save(session);
        return ollamaService.generateQuestion(next, nextOrder, total,
                buildProgressBar(answered, total));
    }

    private Questionnaire loadWithQuestions(BotSession session) {
        return questionnaireRepository
                .findActiveWithQuestions(
                        session.getQuestionnaire().getId(),
                        session.getInstitution().getId()
                )
                .orElse(session.getQuestionnaire());
    }

    private Question findQuestion(List<Question> questions, int order) {
        return questions.stream()
                .filter(q -> q.getOrderNum().equals(order))
                .findFirst()
                .orElse(null);
    }

    private boolean isPauseRequest(String message) {
        String normalized = message.trim().toLowerCase();
        return PAUSE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private String buildProgressBar(int answered, int total) {
        int blocks = 10;
        int filled = total > 0 ? Math.round((float) answered / total * blocks) : 0;
        return "[" + "█".repeat(filled) + "░".repeat(blocks - filled) + "] " +
                (total > 0 ? Math.round((float) answered / total * 100) : 0) + "%";
    }
}