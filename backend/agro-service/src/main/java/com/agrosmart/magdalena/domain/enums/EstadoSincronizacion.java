package com.agrosmart.magdalena.domain.enums;

/**
 * Estados de una operación de sincronización offline.
 * Permite rastrear datos creados sin conexión hasta su persistencia definitiva.
 */
public enum EstadoSincronizacion {
    PENDIENTE,
    EN_PROCESO,
    SINCRONIZADO,
    ERROR
}
