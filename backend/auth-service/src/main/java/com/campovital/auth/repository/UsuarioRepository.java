package com.campovital.auth.repository;

import com.campovital.auth.domain.entity.Usuario;
import com.campovital.auth.domain.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    Long countByActivoTrue();

    @Query("SELECT COUNT(DISTINCT u) FROM Usuario u JOIN u.roles r WHERE u.activo = true AND r.nombre = :rolNombre")
    Long countByRol(@Param("rolNombre") RolNombre rolNombre);
}
