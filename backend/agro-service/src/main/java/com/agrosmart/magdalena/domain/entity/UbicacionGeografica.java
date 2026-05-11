package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Representa una ubicación geográfica.
 * Se usa para georreferenciar fincas y filtrar alertas climáticas por municipio.
 * Datos geográficos del departamento del Magdalena, Colombia.
 */
@Entity
@Table(name = "ubicaciones_geograficas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionGeografica extends BaseEntity {

    @Column(nullable = true)
    private Double latitud;

    @Column(nullable = true)
    private Double longitud;

    @Size(max = 100)
    @Column(length = 100)
    private String vereda;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String municipio;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String departamento = "Magdalena";

    @Size(max = 200)
    @Column(name = "referencia_adicional", length = 200)
    private String referenciaAdicional;
}
