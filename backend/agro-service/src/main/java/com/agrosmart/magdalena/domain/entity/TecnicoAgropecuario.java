package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un técnico agropecuario.
 * Los técnicos participan en el ciclo de recomendaciones técnicas sobre cultivos.
 * Cada técnico tiene una especialidad y zonas (municipios) asignadas.
 */
@Entity
@Table(name = "tecnicos_agropecuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TecnicoAgropecuario extends BaseEntity {

    
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String especialidad;

    @Size(max = 20)
    @Column(name = "numero_registro", length = 20)
    private String numeroRegistro;

    /**
     * Zonas (municipios del Magdalena) asignadas al técnico.
     * Se almacena como lista de strings para filtrado por ubicación del productor.
     */
    @ElementCollection
    @CollectionTable(name = "tecnico_zonas", joinColumns = @JoinColumn(name = "tecnico_id"))
    @Column(name = "zona")
    @Builder.Default
    private List<String> zonasAsignadas = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
