package com.campovital.recommendations.controller;

import com.campovital.recommendations.dto.request.AlertaClimaticaRequest;
import com.campovital.recommendations.dto.response.AlertaClimaticaResponse;
import com.campovital.recommendations.dto.response.ApiResponse;
import com.campovital.recommendations.service.AlertaClimaticaService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alertas Climáticas", description = "Gestión de alertas climáticas")
@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class AlertaClimaticaController {

    private final AlertaClimaticaService alertaService;

    @Operation(summary = "Listar alertas activas")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertaClimaticaResponse>>> listarActivas(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.listarActivas(pageable)));
    }

    @Operation(summary = "Listar historial de alertas")
    @GetMapping("/historial")
    public ResponseEntity<ApiResponse<Page<AlertaClimaticaResponse>>> listarHistorial(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.listarHistorial(pageable)));
    }

    @Operation(summary = "Listar alertas activas por municipio")
    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<ApiResponse<List<AlertaClimaticaResponse>>> listarPorMunicipio(
            @PathVariable String municipio) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.listarPorMunicipio(municipio)));
    }

    @Operation(summary = "Obtener alerta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertaClimaticaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertaService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear nueva alerta climática")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AlertaClimaticaResponse>> crear(
            @Valid @RequestBody AlertaClimaticaRequest request, Authentication auth) {
        AlertaClimaticaResponse response = alertaService.crear(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Desactivar alerta")
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        alertaService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Alerta desactivada", null));
    }
}
