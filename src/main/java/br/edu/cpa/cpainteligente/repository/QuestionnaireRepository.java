package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, String> {

    List<Questionnaire> findByInstitutionId(String institutionId);

    Optional<Questionnaire> findByIdAndInstitutionId(String id, String institutionId);

    // Quando a query é complexa demais para derivar do nome do método,
    // usamos @Query com JPQL (parecido com SQL, mas usa nomes das classes Java)
    @Query("""
        SELECT q FROM Questionnaire q
        JOIN FETCH q.questions questions
        WHERE q.id = :id
        AND q.institution.id = :institutionId
        AND q.active = true
        ORDER BY questions.orderNum ASC
        """)
    Optional<Questionnaire> findActiveWithQuestions(String id, String institutionId);
}
