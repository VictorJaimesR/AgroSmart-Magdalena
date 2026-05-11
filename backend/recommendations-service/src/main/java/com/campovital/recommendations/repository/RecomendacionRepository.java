package com.campovital.recommendations.repository;

import com.campovital.recommendations.domain.entity.Recomendacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    Page<Recomendacion> findByCultivoId(Long cultivoId, Pageable pageable);

    Page<Recomendacion> findByTecnicoId(Long tecnicoId, Pageable pageable);

    @Query("SELECT r FROM Recomendacion r WHERE r.cultivo.parcela.finca.productor.id = :productorId")
    Page<Recomendacion> findByProductorId(@Param("productorId") Long productorId, Pageable pageable);

    @Query("SELECT r FROM Recomendacion r WHERE r.cultivo.parcela.finca.productor.usuarioId = :usuarioId")
    Page<Recomendacion> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    Page<Recomendacion> findByAplicadaFalse(Pageable pageable);
}
