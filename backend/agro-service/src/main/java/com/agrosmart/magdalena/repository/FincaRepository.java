package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.Finca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FincaRepository extends JpaRepository<Finca, Long> {

    Page<Finca> findByProductorIdAndActivoTrue(Long productorId, Pageable pageable);

    List<Finca> findByProductorIdAndActivoTrue(Long productorId);

    @Query("SELECT f FROM Finca f WHERE f.ubicacion.municipio = :municipio AND f.activo = true")
    List<Finca> findByMunicipio(@Param("municipio") String municipio);

    Page<Finca> findByActivoTrue(Pageable pageable);

    @Query("SELECT f FROM Finca f WHERE f.productor.usuarioId = :usuarioId AND f.activo = true")
    Page<Finca> findByProductorUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    long countByProductorIdAndActivoTrue(Long productorId);
}
