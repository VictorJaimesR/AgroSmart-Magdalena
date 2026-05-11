package com.agrosmart.magdalena.domain.event;

import com.agrosmart.magdalena.domain.entity.Cultivo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CultivoStateChangedEvent {
    private Cultivo cultivo;
    private String estadoAnterior;
    private String estadoNuevo;
}
