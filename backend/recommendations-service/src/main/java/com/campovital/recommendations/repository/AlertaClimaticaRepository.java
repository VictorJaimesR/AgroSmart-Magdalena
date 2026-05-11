package com.campovital.recommendations.repository;

import com.campovital.recommendations.domain.entity.AlertaClimatica;
import com.campovital.recommendations.domain.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaClimaticaRepository extends JpaRepository<AlertaClimatica, Long> {

    Page<AlertaClimatica> findByActivaTrue(Pageable pageable);

    Page<AlertaClimatica> findByTipoAndActivaTrue(TipoAlerta tipo, Pageable pageable);

    @Query("SELECT a FROM AlertaClimatica a JOIN a.municipiosAfectados m WHERE m = :municipio AND a.activa = true")
    List<AlertaClimatica> findActivasByMunicipio(@Param("municipio") String municipio);

    Page<AlertaClimatica> findAllByOrderByFechaEmisionDesc(Pageable pageable);
}
