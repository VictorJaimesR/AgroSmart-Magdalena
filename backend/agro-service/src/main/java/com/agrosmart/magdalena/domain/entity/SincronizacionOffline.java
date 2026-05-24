package com.agrosmart.magdalena.domain.entity;

import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sincronizaciones_offline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SincronizacionOffline extends BaseEntity {

    // Guardamos solo el ID del usuario (viene del gateway via header X-User-Id)
    // No usamos @ManyToOne para no acoplar este microservicio a la tabla usuarios
    @NotNull
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Size(max = 80)
    @Column(name = "client_id", length = 80)
    private String clientId;

    @Column(name = "server_id")
    private Long serverId;

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