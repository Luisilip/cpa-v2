package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.dto.request.CreateStudentRequest;
import br.edu.cpa.cpainteligente.dto.response.StudentResponse;
import br.edu.cpa.cpainteligente.security.AuthUtils;
import br.edu.cpa.cpainteligente.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<StudentResponse> listAll() {
        return studentService.listAll(AuthUtils.getInstitutionId());
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable String id) {
        return studentService.findById(id, AuthUtils.getInstitutionId());
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@RequestBody @Valid CreateStudentRequest request) {
        StudentResponse response = studentService.create(request, AuthUtils.getInstitutionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public StudentResponse update(
            @PathVariable String id,
            @RequestBody @Valid CreateStudentRequest request
    ) {
        return studentService.update(id, request, AuthUtils.getInstitutionId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        studentService.delete(id, AuthUtils.getInstitutionId());
        return ResponseEntity.noContent().build();
    }
}