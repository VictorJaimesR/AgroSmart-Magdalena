package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.Parcela;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

    Page<Parcela> findByFincaIdAndActivoTrue(Long fincaId, Pageable pageable);

    List<Parcela> findByFincaIdAndActivoTrue(Long fincaId);

    long countByFincaIdAndActivoTrue(Long fincaId);

    /**
     * Suma el área total de parcelas activas por fincaId, excluyendo parcelas inactivas.
     *
     * @param fincaId ID de la finca
     * @return Suma de áreas de parcelas activas
     */
    @Query("SELECT COALESCE(SUM(p.areaParcela), 0.0) FROM Parcela p WHERE p.finca.id = :fincaId AND p.activo = true")
    Double sumAreaByFincaId(@Param("fincaId") Long fincaId);
}
