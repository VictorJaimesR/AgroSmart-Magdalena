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
public class ParcelaResponse {
    private Long id;
    private String nombre;
    private Double areaParcela;
    private String unidadArea;
    private String tipoSuelo;
    private String descripcion;
    private String estado;  // DISPONIBLE, OCUPADA, INACTIVA
    private Boolean activo;
    private LocalDateTime createdAt;

    // Finca
    private Long fincaId;
    private String fincaNombre;

    // Métricas
    private Long cantidadCultivos;
}
