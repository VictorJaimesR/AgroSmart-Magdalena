package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.Productor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, Long> {

    Optional<Productor> findByUsuarioId(Long usuarioId);

    Optional<Productor> findByEmail(String email);

    Optional<Productor> findByCedula(String cedula);

    Boolean existsByCedula(String cedula);

    Page<Productor> findByActivoTrue(Pageable pageable);

    @Query("SELECT p FROM Productor p WHERE p.asociacion.id = :asociacionId AND p.activo = true")
    Page<Productor> findByAsociacionId(@Param("asociacionId") Long asociacionId, Pageable pageable);
}
