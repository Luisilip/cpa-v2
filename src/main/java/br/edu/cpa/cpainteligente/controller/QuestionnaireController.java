package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.dto.request.CreateQuestionnaireRequest;
import br.edu.cpa.cpainteligente.dto.response.QuestionnaireResponse;
import br.edu.cpa.cpainteligente.security.AuthUtils;
import br.edu.cpa.cpainteligente.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @GetMapping
    public List<QuestionnaireResponse> listAll() {
        return questionnaireService.listAll(AuthUtils.getInstitutionId());
    }

    @PostMapping
    public ResponseEntity<QuestionnaireResponse> create(
            @RequestBody @Valid CreateQuestionnaireRequest request
    ) {
        QuestionnaireResponse response =
                questionnaireService.create(request, AuthUtils.getInstitutionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/toggle-active")
    public QuestionnaireResponse toggleActive(@PathVariable String id) {
        return questionnaireService.toggleActive(id, AuthUtils.getInstitutionId());
    }
}