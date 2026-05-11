package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa una operación de sincronización offline.
 * Cuando un usuario trabaja sin conexión, las operaciones se almacenan
 * localmente y luego se sincronizan con el servidor.
 *
 * Almacena el JSON completo de la operación pendiente con metadata
 * de la entidad destino (tabla, acción CRUD).
 */
@Entity
@Table(name = "sincronizaciones_offline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SincronizacionOffline extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String entidad;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String accion; // CREATE, UPDATE, DELETE

    @NotNull
    @Column(name = "datos_json", nullable = false, columnDefinition = "TEXT")
    private String datosJson;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSincronizacion estado = EstadoSincronizacion.PENDIENTE;

    @Column(name = "fecha_sincronizacion")
    private LocalDateTime fechaSincronizacion;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;
}
