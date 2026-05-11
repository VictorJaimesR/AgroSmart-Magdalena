package com.campovital.recommendations.repository;

import com.campovital.recommendations.domain.entity.HistorialRecomendacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialRecomendacionRepository extends JpaRepository<HistorialRecomendacion, Long> {
    Page<HistorialRecomendacion> findByRecomendacionId(Long recomendacionId, Pageable pageable);
}
