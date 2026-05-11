package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametroTecnicoRequest {

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 100)
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    private String valor;

    @Size(max = 300)
    private String descripcion;

    @Size(max = 50)
    private String categoria;
}
