package com.campovital.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta API estandarizada.
 * Todas las respuestas del backend siguen este formato para consistencia.
 *
 * @param <T> Tipo de datos contenidos en la respuesta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean exitoso;
    private String mensaje;
    private T datos;

    public static <T> ApiResponse<T> ok(T datos) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje("Operación exitosa")
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> ok(String mensaje, T datos) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> created(T datos) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje("Recurso creado exitosamente")
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
                .exitoso(false)
                .mensaje(mensaje)
                .build();
    }
}
