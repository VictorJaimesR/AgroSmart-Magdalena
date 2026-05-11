package com.campovital.recommendations.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CultivoStateChangedRequest {
    private Long cultivoId;
    private String nuevoEstado;
}
