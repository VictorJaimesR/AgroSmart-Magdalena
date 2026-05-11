package com.agrosmart.magdalena.domain.enums;

/**
 * Estados posibles de un cultivo durante su ciclo de vida.
 *
 * Estados activos (mantienen parcela OCUPADA):
 *   - PLANIFICADO, SEMBRADO, EN_CRECIMIENTO, EN_COSECHA
 *
 * Estados terminales (liberan parcela a DISPONIBLE):
 *   - COSECHADO, FINALIZADO, CANCELADO, ABANDONADO
 */
public enum EstadoCultivo {
    // Estados activos
    PLANIFICADO,
    SEMBRADO,
    EN_CRECIMIENTO,
    EN_COSECHA,
    ACTIVO,
    
    // Estados terminales
    COSECHADO,
    FINALIZADO,
    CANCELADO,
    ABANDONADO
}
