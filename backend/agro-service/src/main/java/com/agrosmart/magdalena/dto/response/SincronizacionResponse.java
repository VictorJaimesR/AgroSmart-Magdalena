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
public class SincronizacionResponse {
    private Long id;
    private String clientId;
    private String localId;
    private Long serverId;
    private String entidad;
    private String accion;
    private String estado;
    private String mensajeError;
    private LocalDateTime createdAt;
    private LocalDateTime fechaSincronizacion;
}
