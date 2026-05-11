package com.campovital.recommendations.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
