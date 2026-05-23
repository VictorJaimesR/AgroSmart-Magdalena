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
public class ActividadResponse {

    private Long id;

    // Finca
    private Long fincaId;
    private String fincaNombre;

    // Cultivo
    private Long cultivoId;
    private String cultivoNombre;
    private String cultivoVariedad;

    // Actividad
    private String tipoActividad;
    private Double cantidad;
    private String unidad;
    private String producto;
    private String observaciones;

    // Auditoría
    private Long registradoPorId;
    private String registradoPorNombre;
    private String registradoPorRol;

    private LocalDateTime fechaActividad;
    private LocalDateTime createdAt;
}
