package br.edu.cpa.cpainteligente.dto.response;

import br.edu.cpa.cpainteligente.entity.Student;

import java.time.Instant;

public record StudentResponse(
        String id,
        String name,
        String email,
        String course,
        String className,
        String period,
        Instant createdAt
) {
    // Factory method: converte Entity → DTO
    // Evita depender de frameworks de mapeamento externos
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCourse(),
                student.getClassName(),
                student.getPeriod(),
                student.getCreatedAt()
        );
    }
}
