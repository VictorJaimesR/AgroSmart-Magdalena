package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Tableros de Control (Dashboards)", description = "Estadísticas por rol de usuario")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Métricas para productor")
    @GetMapping("/productor")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductorDashboard(
            Authentication auth,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long usuarioId = null;
        try {
            if (userIdHeader != null) usuarioId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException ignored) {}
        return ResponseEntity.ok(ApiResponse.ok(
            "Métricas de Productor",
            dashboardService.getProductorStats(auth.getName(), usuarioId)));
    }

    @Operation(summary = "Métricas para asociación")
    @GetMapping("/asociacion")
    @PreAuthorize("hasAnyRole('ASOCIACION', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAsociacionDashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Métricas de Asociación", dashboardService.getAsociacionStats()));
    }

    @Operation(summary = "Métricas generales (Admin)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Métricas Globales", dashboardService.getAdminStats()));
    }
}
