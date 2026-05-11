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
public class FincaResponse {
    private Long id;
    private String nombre;
    private Double areaTotal;
    private String unidadArea;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime createdAt;

    // Ubicación
    private Long ubicacionId;
    private Double latitud;
    private Double longitud;
    private String vereda;
    private String municipio;
    private String departamento;

    // Productor
    private Long productorId;
    private String productorNombre;

    // Métricas
    private Long cantidadParcelas;
}
