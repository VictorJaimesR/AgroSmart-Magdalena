package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Representa un administrador del sistema.
 * El administrador configura parámetros técnicos, emite alertas climáticas
 * y gestiona la plataforma.
 */
@Entity
@Table(name = "administradores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrador extends BaseEntity {

    
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String cargo;

    @Size(max = 100)
    @Column(length = 100)
    private String departamento;
}
