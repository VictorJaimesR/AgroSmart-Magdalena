package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.ActividadAgricola;
import com.agrosmart.magdalena.domain.enums.TipoActividad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<ActividadAgricola, Long> {

    /** Historial completo de una finca, ordenado por fecha de actividad descendente */
    Page<ActividadAgricola> findByFincaIdOrderByFechaActividadDesc(Long fincaId, Pageable pageable);

    /** Historial de un cultivo específico */
    Page<ActividadAgricola> findByCultivoIdOrderByFechaActividadDesc(Long cultivoId, Pageable pageable);

    /** Historial de una finca filtrado por tipo de actividad */
    Page<ActividadAgricola> findByFincaIdAndTipoActividadOrderByFechaActividadDesc(
            Long fincaId, TipoActividad tipoActividad, Pageable pageable);

    /** Conteo de actividades por tipo para una finca (resumen/dashboard) */
    @Query("SELECT a.tipoActividad, COUNT(a) FROM ActividadAgricola a WHERE a.finca.id = :fincaId GROUP BY a.tipoActividad")
    List<Object[]> contarPorTipoEnFinca(@Param("fincaId") Long fincaId);

    /** Últimas actividades registradas por un usuario (agricultor o técnico) */
    Page<ActividadAgricola> findByRegistradoPorIdOrderByFechaActividadDesc(Long registradoPorId, Pageable pageable);
}
