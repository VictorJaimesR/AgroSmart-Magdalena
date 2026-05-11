package com.agrosmart.magdalena.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un productor agrícola del Magdalena.
 * Un productor es un usuario con rol PRODUCTOR que posee fincas y solicita reportes.
 * Puede pertenecer opcionalmente a una asociación agrícola.
 *
 * Regla de negocio: Un productor puede tener múltiples fincas.
 */
@Entity
@Table(name = "productores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Productor extends BaseEntity {

    
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @NotBlank
    @Size(max = 200)
    @Column(length = 200)
    private String nombreCompleto;

    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asociacion_id")
    private AsociacionAgricola asociacion;

    @OneToMany(mappedBy = "productor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Finca> fincas = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
