package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una asociación agrícola del Magdalena.
 * Agrupa productores de una misma zona geográfica para facilitar
 * la gestión colectiva y el acceso a recursos.
 */
@Entity
@Table(name = "asociaciones_agricolas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsociacionAgricola extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 20)
    @Column(unique = true, length = 20)
    private String nit;

    @Size(max = 100)
    @Column(length = 100)
    private String municipio;

    @Size(max = 100)
    @Column(length = 100)
    private String representanteLegal;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @OneToMany(mappedBy = "asociacion")
    @Builder.Default
    private List<Productor> productores = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
