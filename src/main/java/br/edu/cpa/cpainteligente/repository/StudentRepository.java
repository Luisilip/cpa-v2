package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    // Gera: SELECT * FROM students WHERE institution_id = ?
    List<Student> findByInstitutionId(String institutionId);

    // Gera: SELECT * FROM students WHERE id = ? AND institution_id = ?
    // Garante que o coordenador só acessa alunos da sua própria instituição
    Optional<Student> findByIdAndInstitutionId(String id, String institutionId);

    Optional<Student> findByPhoneHashAndInstitutionId(String phoneHash, String institutionId);
}
