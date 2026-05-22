package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.FincaDisponibleTecnicoResponse;
import com.agrosmart.magdalena.dto.response.FincaSupervisadaResponse;
import com.agrosmart.magdalena.dto.response.SupervisionFincaResponse;
import com.agrosmart.magdalena.service.SupervisionFincaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Supervisión de Fincas", description = "Operaciones para que técnicos supervisen fincas")
@RestController
@RequestMapping("/supervisiones")
@RequiredArgsConstructor
public class SupervisionFincaController {

    private final SupervisionFincaService supervisionService;

    @Operation(summary = "Listar fincas disponibles para supervisión")
    @GetMapping("/fincas-disponibles")
    @PreAuthorize("hasAnyRole('TECNICO','ADMIN')")
    public ResponseEntity<ApiResponse<List<FincaDisponibleTecnicoResponse>>> listarDisponibles() {
        return ResponseEntity.ok(ApiResponse.ok("Fincas disponibles", supervisionService.listarFincasDisponibles()));
    }

    @Operation(summary = "Tomar una finca para supervisar")
    @PostMapping("/fincas/{fincaId}/tomar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<ApiResponse<SupervisionFincaResponse>> tomarFinca(
            Authentication auth,
            @PathVariable Long fincaId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        Long tecnicoId = null;
        try { if (userIdHeader != null) tecnicoId = Long.valueOf(userIdHeader); } catch (NumberFormatException ignored) {}
        if (tecnicoId == null) return ResponseEntity.badRequest().body(ApiResponse.error("Header X-User-Id requerido"));

        try {
            SupervisionFincaResponse res = supervisionService.tomarFinca(fincaId, tecnicoId, userName, userEmail);
            return ResponseEntity.ok(ApiResponse.created(res));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Listar mis fincas supervisadas (técnico autenticado)")
    @GetMapping("/mis-fincas")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<ApiResponse<List<FincaSupervisadaResponse>>> listarMisFincas(
            Authentication auth,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        Long tecnicoId = null;
        try { if (userIdHeader != null) tecnicoId = Long.valueOf(userIdHeader); } catch (NumberFormatException ignored) {}
        if (tecnicoId == null) return ResponseEntity.badRequest().body(ApiResponse.error("Header X-User-Id requerido"));

        return ResponseEntity.ok(ApiResponse.ok("Mis fincas supervisadas", supervisionService.listarMisFincas(tecnicoId)));
    }

    @Operation(summary = "Finalizar una supervisión")
    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<ApiResponse<SupervisionFincaResponse>> finalizar(
            Authentication auth,
            @PathVariable Long id
    ) {
        try {
            SupervisionFincaResponse res = supervisionService.finalizar(id);
            return ResponseEntity.ok(ApiResponse.ok("Supervisión finalizada", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
