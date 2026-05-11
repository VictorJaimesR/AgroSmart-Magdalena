package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRequest {

    @NotNull(message = "El tipo de reporte es obligatorio")
    private String tipo;

    @Size(max = 200)
    private String titulo;

    private LocalDate periodoInicio;
    private LocalDate periodoFin;
}
