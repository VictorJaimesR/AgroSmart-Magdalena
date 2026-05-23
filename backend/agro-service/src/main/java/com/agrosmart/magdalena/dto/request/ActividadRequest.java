package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRequest {

    @NotNull(message = "El ID de finca es obligatorio")
    private Long fincaId;

    @NotNull(message = "El ID de cultivo es obligatorio")
    private Long cultivoId;

    @NotNull(message = "El tipo de actividad es obligatorio")
    private String tipoActividad;

    @Positive(message = "La cantidad debe ser positiva")
    private Double cantidad;

    @Size(max = 30)
    private String unidad;

    /** Nombre del insumo/producto utilizado */
    @Size(max = 150)
    private String producto;

    @Size(max = 1000)
    private String observaciones;

    @NotNull(message = "La fecha de la actividad es obligatoria")
    private LocalDateTime fechaActividad;
}
