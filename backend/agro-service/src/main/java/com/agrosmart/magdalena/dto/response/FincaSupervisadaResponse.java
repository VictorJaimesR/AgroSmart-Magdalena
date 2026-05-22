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
public class FincaSupervisadaResponse {
    private Long supervisionId;
    private Long fincaId;
    private String fincaNombre;
    private Long productorUsuarioId;
    private String productorNombre;
    private String municipio;
    private Double areaTotal;
    private LocalDateTime fechaInicio;
    private String estado;
}
