package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Representa un parámetro técnico configurable del sistema.
 * Solo los administradores pueden modificar estos parámetros.
 * Ejemplos: umbrales de alerta, intervalos de sincronización, etc.
 */
@Entity
@Table(name = "parametros_tecnicos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "clave")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroTecnico extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String clave;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Size(max = 300)
    @Column(length = 300)
    private String descripcion;

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modificado_por")
    private Usuario modificadoPor;
}
