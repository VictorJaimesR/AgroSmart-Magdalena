package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.request.ActividadRequest;
import com.agrosmart.magdalena.dto.response.ActividadResponse;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.service.ActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Actividades Agrícolas", description = "Registro y consulta de actividades realizadas en cultivos")
@RestController
@RequestMapping("/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    @Operation(summary = "Registrar una actividad agrícola en un cultivo")
    @PostMapping
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<ActividadResponse>> registrar(
            @Valid @RequestBody ActividadRequest request,
            @RequestHeader(value = "X-User-Id",    required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role",  required = false) String userRole
    ) {
        Long userId = null;
        try {
            if (userIdHeader != null) userId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException ignored) {}

        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Header X-User-Id requerido"));
        }

        // Determinar rol principal (gateway envía "ROLE_TECNICO", etc.)
        String rol = "AGRICULTOR";
        if (userRole != null) {
            if (userRole.contains("TECNICO"))    rol = "TECNICO";
            else if (userRole.contains("ADMIN")) rol = "ADMIN";
        }

        String nombre = (userEmail != null && !userEmail.isBlank()) ? userEmail : "Usuario #" + userId;

        ActividadResponse response = actividadService.registrar(request, userId, nombre, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Historial de actividades de una finca (todos sus cultivos)")
    @GetMapping("/finca/{fincaId}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActividadResponse>>> listarPorFinca(
            @PathVariable Long fincaId,
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ActividadResponse> page = (tipo != null && !tipo.isBlank())
                ? actividadService.listarPorFincaYTipo(fincaId, tipo, pageable)
                : actividadService.listarPorFinca(fincaId, pageable);

        return ResponseEntity.ok(ApiResponse.ok("Historial de actividades", page));
    }

    @Operation(summary = "Historial de actividades de un cultivo específico")
    @GetMapping("/cultivo/{cultivoId}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ActividadResponse>>> listarPorCultivo(
            @PathVariable Long cultivoId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Historial del cultivo", actividadService.listarPorCultivo(cultivoId, pageable)));
    }

    @Operation(summary = "Resumen de actividades por tipo para una finca (para dashboard)")
    @GetMapping("/finca/{fincaId}/resumen")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'TECNICO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> resumenPorFinca(@PathVariable Long fincaId) {
        return ResponseEntity.ok(ApiResponse.ok("Resumen de actividades", actividadService.resumenPorFinca(fincaId)));
    }
}
