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
public class FincaRequest {

    @NotBlank(message = "El nombre de la finca es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotNull(message = "El área total es obligatoria")
    @Positive(message = "El área debe ser positiva")
    private Double areaTotal;

    @Size(max = 20)
    private String unidadArea;

    @Size(max = 500)
    private String descripcion;

    // --- Ubicación geográfica ---
    private Double latitud;

    private Double longitud;

    @Size(max = 100)
    private String vereda;

    @NotBlank(message = "El municipio es obligatorio")
    @Size(max = 100)
    private String municipio;

    @Size(max = 200)
    private String referenciaAdicional;
}
