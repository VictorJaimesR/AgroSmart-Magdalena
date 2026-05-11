package com.agrosmart.magdalena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelaRequest {

    @NotBlank(message = "El nombre de la parcela es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotNull(message = "El ID de finca es obligatorio")
    private Long fincaId;

    @NotNull(message = "El área de la parcela es obligatoria")
    @Positive(message = "El área debe ser positiva")
    private Double areaParcela;

    @Size(max = 20)
    private String unidadArea;

    @Size(max = 50)
    private String tipoSuelo;

    @Size(max = 500)
    private String descripcion;
}
