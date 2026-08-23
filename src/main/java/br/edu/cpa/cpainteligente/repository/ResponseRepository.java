package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponseRepository extends JpaRepository<Response, String> {

    Optional<Response> findByStudentIdAndQuestionnaireId(
            String studentId,
            String questionnaireId
    );
}
