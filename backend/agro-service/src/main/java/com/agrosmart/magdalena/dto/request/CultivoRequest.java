package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CultivoRequest {

    @NotBlank(message = "El nombre del cultivo es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String variedad;

    @NotNull(message = "El ID de parcela es obligatorio")
    private Long parcelaId;

    @NotNull(message = "La fecha de siembra es obligatoria")
    private LocalDate fechaSiembra;

    private LocalDate fechaCosechaEstimada;

    private String estado;

    @Positive(message = "El área debe ser positiva")
    private Double areaUtilizada;

    @Size(max = 500)
    private String observaciones;

    private Double rendimientoEsperado;

    @Size(max = 30)
    private String unidadRendimiento;

    private String imagenUrl;
}
