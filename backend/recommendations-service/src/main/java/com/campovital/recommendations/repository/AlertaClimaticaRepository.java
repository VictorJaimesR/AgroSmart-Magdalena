package com.campovital.recommendations.repository;

import com.campovital.recommendations.domain.entity.AlertaClimatica;
import com.campovital.recommendations.domain.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertaClimaticaRepository extends JpaRepository<AlertaClimatica, Long> {

    Page<AlertaClimatica> findByActivaTrue(Pageable pageable);

    Page<AlertaClimatica> findAllByOrderByFechaEmisionDesc(Pageable pageable);

    @Query("SELECT a FROM AlertaClimatica a WHERE a.activa = true " +
            "AND :municipio MEMBER OF a.municipiosAfectados")
    List<AlertaClimatica> findActivasByMunicipio(@Param("municipio") String municipio);

    @Query("SELECT COUNT(a) > 0 FROM AlertaClimatica a " +
            "WHERE :municipio MEMBER OF a.municipiosAfectados " +
            "AND a.tipo = :tipo " +
            "AND a.fechaEmision >= :desde " +
            "AND a.activa = true")
    boolean existeAlertaReciente(
            @Param("municipio") String municipio,
            @Param("tipo") TipoAlerta tipo,
            @Param("desde") LocalDateTime desde);
}