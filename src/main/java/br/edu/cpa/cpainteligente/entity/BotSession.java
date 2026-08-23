package br.edu.cpa.cpainteligente.entity;

import br.edu.cpa.cpainteligente.entity.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "bot_sessions")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotSession {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    // Qual pergunta o aluno está respondendo agora (por ordem)
    @Builder.Default
    @Column(name = "current_question_order", nullable = false)
    private Integer currentQuestionOrder = 1;

    // Hash do telefone — não armazenamos o número real
    @Column(name = "phone_hash", nullable = false)
    private String phoneHash;

    @Column(name = "last_message_at", nullable = false)
    @Builder.Default
    private Instant lastMessageAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id")
    private Response response;
}