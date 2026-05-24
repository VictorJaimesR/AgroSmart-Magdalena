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

    Page<ActividadAgricola> findByFincaIdOrderByFechaActividadDesc(Long fincaId, Pageable pageable);

    Page<ActividadAgricola> findByCultivoIdOrderByFechaActividadDesc(Long cultivoId, Pageable pageable);

    Page<ActividadAgricola> findByFincaIdAndTipoActividadOrderByFechaActividadDesc(
            Long fincaId, TipoActividad tipoActividad, Pageable pageable);

    @Query("SELECT a.tipoActividad, COUNT(a) FROM ActividadAgricola a WHERE a.finca.id = :fincaId GROUP BY a.tipoActividad")
    List<Object[]> contarPorTipoEnFinca(@Param("fincaId") Long fincaId);

    Page<ActividadAgricola> findByRegistradoPorIdOrderByFechaActividadDesc(Long registradoPorId, Pageable pageable);

    /** Todas las actividades de las fincas de un productor */
    @Query("SELECT a FROM ActividadAgricola a WHERE a.finca.productor.usuarioId = :usuarioId ORDER BY a.fechaActividad DESC")
    List<ActividadAgricola> findByProductorUsuarioId(@Param("usuarioId") Long usuarioId);

    /** Resumen de insumos: tipo, producto, unidad y total consumido por productor */
    @Query("""
        SELECT a.tipoActividad, a.producto, a.unidad, SUM(a.cantidad), COUNT(a),
               a.finca.nombre, a.cultivo.nombre
        FROM ActividadAgricola a
        WHERE a.finca.productor.usuarioId = :usuarioId
          AND a.producto IS NOT NULL
          AND a.cantidad IS NOT NULL
        GROUP BY a.tipoActividad, a.producto, a.unidad, a.finca.nombre, a.cultivo.nombre
        ORDER BY a.tipoActividad, a.producto
        """)
    List<Object[]> resumenInsumosPorProductor(@Param("usuarioId") Long usuarioId);

    /** Resumen de todas las actividades por tipo (incluso sin producto) por productor */
    @Query("""
        SELECT a.tipoActividad, a.finca.nombre, a.cultivo.nombre,
               COUNT(a), SUM(CASE WHEN a.cantidad IS NOT NULL THEN a.cantidad ELSE 0 END), a.unidad
        FROM ActividadAgricola a
        WHERE a.finca.productor.usuarioId = :usuarioId
        GROUP BY a.tipoActividad, a.finca.nombre, a.cultivo.nombre, a.unidad
        ORDER BY a.finca.nombre, a.tipoActividad
        """)
    List<Object[]> resumenActividadesPorProductor(@Param("usuarioId") Long usuarioId);
}