package com.agrosmart.magdalena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FincaDisponibleTecnicoResponse {
    private Long id;
    private String nombre;
    private Double areaTotal;
    private String municipio;
    private String vereda;
    private Long productorUsuarioId;
    private String productorNombre;
}
