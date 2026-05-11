package com.campovital.auth.repository;

import com.campovital.auth.domain.entity.Rol;
import com.campovital.auth.domain.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolNombre nombre);

    Boolean existsByNombre(RolNombre nombre);
}
