package br.edu.cpa.cpainteligente.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// record: imutável, gera construtor/getters/equals/toString automaticamente
// @param com anotações de validação — Spring valida antes de chegar no controller
public record RegisterRequest(

        @NotBlank(message = "Nome da instituição é obrigatório")
        String institutionName,

        @NotBlank(message = "Seu nome é obrigatório")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        String password
) {}