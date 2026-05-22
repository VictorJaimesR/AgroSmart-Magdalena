package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.SupervisionFinca;
import com.agrosmart.magdalena.domain.enums.EstadoSupervision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisionFincaRepository extends JpaRepository<SupervisionFinca, Long> {

    boolean existsByFincaIdAndEstado(Long fincaId, EstadoSupervision estado);

    List<SupervisionFinca> findByTecnicoUsuarioIdAndEstado(Long tecnicoUsuarioId, EstadoSupervision estado);

}
