package com.agrosmart.magdalena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CultivoResponse {
    private Long id;
    private String nombre;
    private String variedad;
    private LocalDate fechaSiembra;
    private LocalDate fechaCosechaEstimada;
    private LocalDate fechaCosechaReal;
    private String estado;
    private Double areaUtilizada;
    private String observaciones;
    private Double rendimientoEsperado;
    private Double rendimientoReal;
    private String unidadRendimiento;
    private Boolean activo;
    private LocalDateTime createdAt;
    private String imagenUrl;

    // Parcela
    private Long parcelaId;
    private String parcelaNombre;

    // Finca (para contexto)
    private Long fincaId;
    private String fincaNombre;
}
