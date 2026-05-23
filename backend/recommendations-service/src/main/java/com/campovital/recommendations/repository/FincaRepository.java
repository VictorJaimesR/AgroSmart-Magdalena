package com.campovital.recommendations.repository;

import com.campovital.recommendations.domain.entity.Finca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FincaRepository extends JpaRepository<Finca, Long> {
}