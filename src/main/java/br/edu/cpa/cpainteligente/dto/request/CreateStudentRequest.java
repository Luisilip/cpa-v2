package br.edu.cpa.cpainteligente.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateStudentRequest(

        @NotBlank(message = "Nome do aluno é obrigatório")
        String name,

        String email,
        String phone,
        String course,
        String className,
        String period
) {}