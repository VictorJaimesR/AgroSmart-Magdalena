package com.agrosmart.magdalena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para vista detallada de finca en ADMIN.
 * Incluye información completa de parcelas y cultivos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFincaDetalleResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double areaTotal;
    private String unidadArea;
    
    // Ubicación completa
    private Double latitud;
    private Double longitud;
    private String vereda;
    private String municipio;
    private String departamento;
    
    // Agricultor dueño (información completa)
    private Long productorId;
    private String productorNombre;
    private String productorCedula;
    private String productorEmail;
    private String productorTelefono;
    
    // Técnico asociado (por municipio)
    private Long tecnicoId;
    private String tecnicoNombre;
    private String tecnicoEspecialidad;
    
    // Estado
    private String estado;
    
    // Métricas
    private Long cantidadParcelas;
    private Long cantidadCultivos;
    private Double areaCultivada;
    
    // Parcelas y cultivos detallados
    private List<ParcelaDetalleDto> parcelas;
    
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParcelaDetalleDto {
        private Long id;
        private String nombre;
        private Double areaParcela;
        private String unidadArea;
        private String tipoSuelo;
        private String descripcion;
        private String estado;
        private List<CultivoDetalleDto> cultivos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CultivoDetalleDto {
        private Long id;
        private String nombre;
        private String variedad;
        private LocalDate fechaSiembra;
        private LocalDate fechaCosechaEstimada;
        private LocalDate fechaCosechaReal;
        private Double areaUtilizada;
        private String estado;
        private Double rendimientoEsperado;
        private Double rendimientoReal;
        private String unidadRendimiento;
        private String observaciones;
    }
}
