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
public class ParametroTecnicoResponse {
    private Long id;
    private String clave;
    private String valor;
    private String descripcion;
    private String categoria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String modificadoPor;
}
