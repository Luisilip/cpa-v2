package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // Spring lê o nome do método e gera o SQL automaticamente:
    // "findBy" + "Email" → SELECT * FROM users WHERE email = ?
    // Optional<> porque pode não encontrar nenhum usuário
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
