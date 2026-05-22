package com.agrosmart.magdalena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupervisionFincaResponse {
    private Long id;
    private Long fincaId;
    private String fincaNombre;
    private Long tecnicoUsuarioId;
    private String tecnicoNombre;
    private String tecnicoEmail;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
}
