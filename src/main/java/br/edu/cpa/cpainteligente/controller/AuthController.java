package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.dto.request.LoginRequest;
import br.edu.cpa.cpainteligente.dto.request.RegisterRequest;
import br.edu.cpa.cpainteligente.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.edu.cpa.cpainteligente.service.AuthService;

// @RestController = @Controller + @ResponseBody
// Significa: esta classe lida com HTTP e retorna JSON automaticamente
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @Valid dispara as validações do DTO antes de entrar no método
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
