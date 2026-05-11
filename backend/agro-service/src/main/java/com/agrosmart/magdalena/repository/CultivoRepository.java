package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.Cultivo;
import com.agrosmart.magdalena.domain.enums.EstadoCultivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    Page<Cultivo> findByParcelaIdAndActivoTrue(Long parcelaId, Pageable pageable);

    List<Cultivo> findByParcelaIdAndActivoTrue(Long parcelaId);

    Page<Cultivo> findByEstadoAndActivoTrue(EstadoCultivo estado, Pageable pageable);

    @Query("SELECT c FROM Cultivo c WHERE c.parcela.finca.productor.id = :productorId AND c.activo = true")
    Page<Cultivo> findByProductorId(@Param("productorId") Long productorId, Pageable pageable);

    @Query("SELECT c FROM Cultivo c WHERE c.parcela.finca.id = :fincaId AND c.activo = true")
    Page<Cultivo> findByFincaId(@Param("fincaId") Long fincaId, Pageable pageable);

    @Query("SELECT SUM(c.areaUtilizada) FROM Cultivo c WHERE c.parcela.id = :parcelaId AND c.activo = true")
    Double sumAreaUtilizadaByParcelaId(@Param("parcelaId") Long parcelaId);

    @Query("SELECT c FROM Cultivo c WHERE c.parcela.finca.productor.usuarioId = :usuarioId AND c.activo = true")
    Page<Cultivo> findByProductorUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    Page<Cultivo> findByActivoTrue(Pageable pageable);

    /**
     * Verifica si existe un cultivo activo (no terminal) en una parcela.
     * Estados activos: PLANIFICADO, SEMBRADO, EN_CRECIMIENTO, EN_COSECHA, ACTIVO
     *
     * @param parcelaId ID de la parcela
     * @return true si existe un cultivo activo en la parcela
     */
    @Query("SELECT COUNT(c) > 0 FROM Cultivo c WHERE c.parcela.id = :parcelaId " +
            "AND c.activo = true AND c.estado IN ('PLANIFICADO', 'SEMBRADO', 'EN_CRECIMIENTO', 'EN_COSECHA', 'ACTIVO')")
    boolean existsActiveCultivoByParcelaId(@Param("parcelaId") Long parcelaId);

    /**
     * Obtiene el cultivo activo en una parcela, si existe.
     *
     * @param parcelaId ID de la parcela
     * @return Cultivo activo si existe
     */
    @Query("SELECT c FROM Cultivo c WHERE c.parcela.id = :parcelaId " +
            "AND c.activo = true AND c.estado IN ('PLANIFICADO', 'SEMBRADO', 'EN_CRECIMIENTO', 'EN_COSECHA', 'ACTIVO')")
    Optional<Cultivo> findActiveCultivoByParcelaId(@Param("parcelaId") Long parcelaId);
}
