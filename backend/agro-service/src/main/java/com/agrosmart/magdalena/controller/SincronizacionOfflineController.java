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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Sincronización Offline", description = "Gestión de operaciones pendientes de sincronización")
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SincronizacionOfflineController {

    private final SincronizacionOfflineService syncService;

    @Operation(summary = "Registrar operación offline para sincronización")
    @PostMapping("/push")
    public ResponseEntity<ApiResponse<SincronizacionResponse>> push(
            @Valid @RequestBody SincronizacionRequest request,
            @RequestHeader("X-User-Id") Long usuarioId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", defaultValue = "AGRICULTOR") String rol) {
        SincronizacionResponse response = syncService.registrar(request, usuarioId, email, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Enviar lote de operaciones offline y procesarlas")
    @PostMapping("/push-batch")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> pushBatch(
            @Valid @RequestBody List<SincronizacionRequest> requests,
            @RequestHeader("X-User-Id") Long usuarioId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", defaultValue = "AGRICULTOR") String rol) {

        // 1. Guardar encolados
        List<SincronizacionResponse> registrados = requests.stream()
                .map(r -> syncService.registrar(r, usuarioId, email, rol))
                .toList();

        // 2. Procesar pendientes usando el usuarioId directamente
        List<SincronizacionResponse> procesados = syncService.procesarPendientes(usuarioId, email, rol);

        Map<String, SincronizacionResponse> responses = new LinkedHashMap<>();
        registrados.forEach(r -> responses.put(responseKey(r), r));
        procesados.forEach(r -> responses.put(responseKey(r), r));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(List.copyOf(responses.values())));
    }

    @Operation(summary = "Procesar operaciones pendientes de un usuario")
    @PostMapping("/process/{usuarioId}")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> procesar(
            @PathVariable Long usuarioId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-User-Role", defaultValue = "AGRICULTOR") String rol) {
        List<SincronizacionResponse> results = syncService.procesarPendientes(usuarioId, email, rol);
        return ResponseEntity.ok(ApiResponse.ok("Sincronización completada", results));
    }

    @Operation(summary = "Listar operaciones pendientes")
    @GetMapping("/pending/{usuarioId}")
    public ResponseEntity<ApiResponse<List<SincronizacionResponse>>> listarPendientes(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(syncService.listarPendientes(usuarioId)));
    }

    private String responseKey(SincronizacionResponse response) {
        return response.getClientId() != null ? response.getClientId() : "sync-" + response.getId();
    }
}