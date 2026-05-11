package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SincronizacionRequest {

    @NotBlank(message = "La entidad es obligatoria")
    @Size(max = 50)
    private String entidad;

    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 10)
    private String accion;

    @NotNull(message = "Los datos JSON son obligatorios")
    private String datosJson;
}
