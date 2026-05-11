package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.EstadoParcela;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una parcela o lote dentro de una finca.
 *
 * Reglas de negocio:
 *   - Una parcela pertenece a exactamente una finca
 *   - Una parcela puede contener múltiples cultivos
 *   - La suma de áreas de cultivos no debe exceder el área de la parcela
 */
@Entity
@Table(name = "parcelas", indexes = {
        @Index(name = "idx_parcela_finca", columnList = "finca_id"),
        @Index(name = "idx_parcela_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcela extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finca_id", nullable = false)
    private Finca finca;

    @NotNull
    @Positive
    @Column(name = "area_parcela", nullable = false)
    private Double areaParcela;

    @Size(max = 20)
    @Column(name = "unidad_area", length = 20)
    @Builder.Default
    private String unidadArea = "hectáreas";

    @Size(max = 50)
    @Column(name = "tipo_suelo", length = 50)
    private String tipoSuelo;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoParcela estado = EstadoParcela.DISPONIBLE;

    @OneToMany(mappedBy = "parcela", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Cultivo> cultivos = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
