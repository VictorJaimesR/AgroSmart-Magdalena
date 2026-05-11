package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.Rol;
import com.agrosmart.magdalena.domain.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);

    Boolean existsByNombre(RolNombre nombre);
}
