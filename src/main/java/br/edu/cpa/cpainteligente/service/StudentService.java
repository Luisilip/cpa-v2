package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.dto.request.CreateStudentRequest;
import br.edu.cpa.cpainteligente.dto.response.StudentResponse;
import br.edu.cpa.cpainteligente.entity.Institution;
import br.edu.cpa.cpainteligente.entity.Student;
import br.edu.cpa.cpainteligente.exception.ApiException;
import br.edu.cpa.cpainteligente.repository.InstitutionRepository;
import br.edu.cpa.cpainteligente.repository.StudentRepository;
import br.edu.cpa.cpainteligente.security.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final InstitutionRepository institutionRepository;
    private final HashUtil hashUtil;

    public List<StudentResponse> listAll(String institutionId) {
        return studentRepository
                .findByInstitutionId(institutionId)
                .stream()
                .map(StudentResponse::from)  // method reference — equivale a s -> StudentResponse.from(s)
                .toList();
    }

    public StudentResponse findById(String id, String institutionId) {
        Student student = studentRepository
                .findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> ApiException.notFound("Aluno não encontrado"));

        return StudentResponse.from(student);
    }

    public StudentResponse create(CreateStudentRequest request, String institutionId) {
        // Busca a instituição — precisamos da entidade para setar no relacionamento
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> ApiException.notFound("Instituição não encontrada"));

        Student student = Student.builder()
                .name(request.name())
                .email(request.email() != null ? request.email().toLowerCase() : null)
                .phoneHash(request.phone() != null ? hashUtil.hash(request.phone()) : null)
                .course(request.course())
                .className(request.className())
                .period(request.period())
                .institution(institution)
                .build();

        return StudentResponse.from(studentRepository.save(student));
    }

    public StudentResponse update(String id, CreateStudentRequest request, String institutionId) {
        Student student = studentRepository
                .findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> ApiException.notFound("Aluno não encontrado"));

        // Atualiza só os campos que vieram no request
        if (request.name() != null)      student.setName(request.name());
        if (request.email() != null)     student.setEmail(request.email().toLowerCase());
        if (request.phone() != null) student.setPhoneHash(hashUtil.hash(request.phone()));
        if (request.course() != null)    student.setCourse(request.course());
        if (request.className() != null) student.setClassName(request.className());
        if (request.period() != null)    student.setPeriod(request.period());

        return StudentResponse.from(studentRepository.save(student));
    }

    public void delete(String id, String institutionId) {
        Student student = studentRepository
                .findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> ApiException.notFound("Aluno não encontrado"));

        studentRepository.delete(student);
    }
}