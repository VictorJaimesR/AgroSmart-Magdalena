package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.TecnicoAgropecuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TecnicoAgropecuarioRepository extends JpaRepository<TecnicoAgropecuario, Long> {
    Optional<TecnicoAgropecuario> findByUsuarioId(Long usuarioId);
}
