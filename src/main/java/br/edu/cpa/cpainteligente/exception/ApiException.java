package br.edu.cpa.cpainteligente.exception;

import org.springframework.http.HttpStatus;

// Exceção que carrega o status HTTP junto
// Quando lançada em qualquer camada, o GlobalExceptionHandler
// captura e retorna a resposta correta para o cliente
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    // Métodos estáticos para criar as exceções mais comuns — leitura mais clara
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message);
    }
}
