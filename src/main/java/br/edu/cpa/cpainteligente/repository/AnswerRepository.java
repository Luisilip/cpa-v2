package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, String> {

    Optional<Answer> findByResponseIdAndQuestionId(String responseId, String questionId);

    // Conta quantas respostas já foram salvas nesta avaliação
    long countByResponseId(String responseId);

    // Retorna todas as respostas de uma avaliação (para verificar progresso)
    List<Answer> findByResponseId(String responseId);
}