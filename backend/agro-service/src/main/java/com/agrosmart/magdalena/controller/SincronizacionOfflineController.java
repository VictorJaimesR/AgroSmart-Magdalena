package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.request.SincronizacionRequest;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.SincronizacionResponse;
import com.agrosmart.magdalena.service.SincronizacionOfflineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sincronización Offline", description = "Gestión de operaciones pendientes de sincronización")
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SincronizacionOfflineController {

    private final SincronizacionOfflineService syncService;

    @Operation(summary = "Registrar operación offline para sincronización")
    @PostMapping("/push")
    public ResponseEntity<ApiResponse<SincronizacionResponse>> push(
            @Valid @RequestBody SincronizacionRequest request, Authentication auth) {
        SincronizacionResponse response = syncService.registrar(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Enviar lote de operaciones offline y procesarlas")
    @PostMapping("/push-batch")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> pushBatch(
            @Valid @RequestBody List<SincronizacionRequest> requests, Authentication auth) {
        
        // 1. Guardar encolados
        requests.forEach(r -> syncService.registrar(r, auth.getName()));
        
        // 2. Ejecutar procesamiento atado a la estrategia Server Wins
        // Como sabemos el email, buscamos el usuario via un método auxiliar de procesar o le pasamos el email
        List<SincronizacionResponse> responses = syncService.procesarPendientesPorEmail(auth.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(responses));
    }

    @Operation(summary = "Procesar operaciones pendientes de un usuario")
    @PostMapping("/process/{usuarioId}")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> procesar(
            @PathVariable Long usuarioId) {
        List<SincronizacionResponse> results = syncService.procesarPendientes(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok("Sincronización completada", results));
    }

    @Operation(summary = "Listar operaciones pendientes")
    @GetMapping("/pending/{usuarioId}")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> listarPendientes(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.listarPendientes(usuarioId)));
    }
}
