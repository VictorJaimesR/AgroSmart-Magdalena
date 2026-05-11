package com.campovital.recommendations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaClimaticaResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaExpiracion;
    private List<String> municipiosAfectados;
    private Boolean activa;
    private String estadoAlerta;
    private Long generadaPorId;
    private LocalDateTime createdAt;
    private String emitidaPor;
}
