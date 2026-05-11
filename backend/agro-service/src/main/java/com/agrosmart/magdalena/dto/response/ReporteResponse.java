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
public class ReporteResponse {
    private Long id;
    private String titulo;
    private String tipo;
    private String contenido;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime createdAt;
    private Long productorId;
    private String productorNombre;
}
