package br.edu.cpa.cpainteligente.entity;

import br.edu.cpa.cpainteligente.entity.enums.SinaesAxis;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "questions")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // SEMPRE EnumType.STRING — nunca ORDINAL (reordenar o enum corromperia dados)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SinaesAxis axis;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;
}