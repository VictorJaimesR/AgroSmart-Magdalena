package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.RolNombre;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa un rol del sistema.
 * Los roles determinan los permisos y vistas disponibles para cada usuario.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RolNombre nombre;
}
