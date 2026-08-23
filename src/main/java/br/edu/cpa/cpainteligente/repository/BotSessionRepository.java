package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.BotSession;
import br.edu.cpa.cpainteligente.entity.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BotSessionRepository extends JpaRepository<BotSession, String> {

    // Busca sessão ativa pelo hash do telefone — usado pelo webhook do WhatsApp
    Optional<BotSession> findByPhoneHashAndStatusIn(
            String phoneHash,
            List<SessionStatus> statuses
    );

    // @Modifying = esta query modifica dados (UPDATE/DELETE), não é SELECT
    // @Query com UPDATE nativo — mais eficiente que carregar e atualizar objeto a objeto
    @Modifying
    @Query("""
        UPDATE BotSession s SET s.status = 'EXPIRED'
        WHERE s.status IN ('IN_PROGRESS', 'PAUSED')
          AND s.expiresAt < :now
        """)
    int expireStaleSessionsBefore(Instant now);
}
