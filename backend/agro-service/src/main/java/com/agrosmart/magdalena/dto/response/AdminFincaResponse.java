package com.agrosmart.magdalena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para listado global de fincas en vista ADMIN.
 * Información simplificada sin capacidad de modificación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFincaResponse {
    private Long id;
    private String nombre;
    private Double areaTotal;
    private String unidadArea;
    
    // Ubicación
    private String municipio;
    private String departamento;
    private String vereda;
    
    // Agricultor dueño
    private Long productorId;
    private String productorNombre;
    private String productorCedula;
    
    // Técnico asociado (por municipio)
    private Long tecnicoId;
    private String tecnicoNombre;
    
    // Estado
    private String estado;
    
    // Métricas
    private Long cantidadParcelas;
    private Long cantidadCultivos;
    private Double areaCultivada;
    
    private LocalDateTime createdAt;
}
