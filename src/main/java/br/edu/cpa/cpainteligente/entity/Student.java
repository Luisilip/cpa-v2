package br.edu.cpa.cpainteligente.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "students")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    // Hash do telefone — nunca armazenamos o número real (LGPD)
    @Column(name = "phone_hash")
    private String phoneHash;

    // Segmentação para o dashboard
    @Column
    private String course;

    // "class" é palavra reservada em Java → usamos className
    @Column(name = "class_name")
    private String className;

    @Column
    private String period;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
}