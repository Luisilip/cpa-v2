package br.edu.cpa.cpainteligente.service;

import br.edu.cpa.cpainteligente.dto.request.LoginRequest;
import br.edu.cpa.cpainteligente.dto.request.RegisterRequest;
import br.edu.cpa.cpainteligente.dto.response.AuthResponse;
import br.edu.cpa.cpainteligente.entity.Institution;
import br.edu.cpa.cpainteligente.entity.User;
import br.edu.cpa.cpainteligente.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.cpa.cpainteligente.repository.InstitutionRepository;
import br.edu.cpa.cpainteligente.repository.UserRepository;
import br.edu.cpa.cpainteligente.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // @Transactional: se qualquer coisa falhar dentro deste método,
    // TUDO é desfeito — não fica institution sem user nem user sem institution
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("E-mail já cadastrado");
        }

        // Cria a instituição primeiro
        Institution institution = institutionRepository.save(
                Institution.builder()
                        .name(request.institutionName())
                        .build()
        );

        // Cria o usuário vinculado
        User user = userRepository.save(
                User.builder()
                        .name(request.name())
                        .email(request.email().toLowerCase())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .institution(institution)
                        .build()
        );

        String token = jwtService.generateToken(user.getId(), institution.getId());

        return new AuthResponse(token, user.getId(), user.getName(),
                user.getEmail(), institution.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getId(),
                user.getInstitution().getId());

        return new AuthResponse(token, user.getId(), user.getName(),
                user.getEmail(), user.getInstitution().getId());
    }
}
