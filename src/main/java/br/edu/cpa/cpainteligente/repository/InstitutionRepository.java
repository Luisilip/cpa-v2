package br.edu.cpa.cpainteligente.repository;

import br.edu.cpa.cpainteligente.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Entidade, TipoDoId>
// Herdar isso te dá de graça:
//   save(entity), findById(id), findAll(), deleteById(id),
//   count(), existsById(id) — e mais ~20 métodos
public interface InstitutionRepository extends JpaRepository<Institution, String> {
}
