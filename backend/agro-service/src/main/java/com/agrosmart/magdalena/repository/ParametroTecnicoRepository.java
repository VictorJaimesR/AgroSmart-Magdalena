package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.ParametroTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametroTecnicoRepository extends JpaRepository<ParametroTecnico, Long> {

    Optional<ParametroTecnico> findByClave(String clave);

    List<ParametroTecnico> findByCategoria(String categoria);

    Boolean existsByClave(String clave);
}
