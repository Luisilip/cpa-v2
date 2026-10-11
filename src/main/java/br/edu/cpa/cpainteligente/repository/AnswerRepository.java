package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, String> {

    @Query("SELECT a FROM Answer a JOIN FETCH a.question WHERE a.id = :id")
    Optional<Answer> findByIdWithQuestion(String id);

    Optional<Answer> findByResponseIdAndQuestionId(String responseId, String questionId);

    // Conta quantas respostas já foram salvas nesta avaliação
    long countByResponseId(String responseId);

    // Retorna todas as respostas de uma avaliação (para verificar progresso)
    List<Answer> findByResponseId(String responseId);

    List<Answer> findByCommentIsNotNullAndSentimentIsNullAndResponse_Institution_Id(String institutionId);

    long countByCommentIsNotNullAndResponse_Institution_Id(String institutionId);

    long countByCommentIsNotNullAndSentimentIsNotNullAndResponse_Institution_Id(String institutionId);

    long countByCommentIsNotNullAndSentimentIsNullAndResponse_Institution_Id(String institutionId);
}