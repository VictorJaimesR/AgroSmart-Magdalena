package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.domain.enums.EstadoCultivo;
import com.agrosmart.magdalena.dto.request.CultivoRequest;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.CultivoResponse;
import com.agrosmart.magdalena.service.CultivoService;
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

@Tag(name = "Cultivos", description = "Gestión de cultivos")
@RestController
@RequestMapping("/cultivos")
@RequiredArgsConstructor
public class CultivoController {

    private final CultivoService cultivoService;

    @Operation(summary = "Listar todos los cultivos activos")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CultivoResponse>>> listar(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.listarTodos(pageable)));
    }

    @Operation(summary = "Listar cultivos por parcela")
    @GetMapping("/parcela/{parcelaId}")
    public ResponseEntity<ApiResponse<Page<CultivoResponse>>> listarPorParcela(
            @PathVariable Long parcelaId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.listarPorParcela(parcelaId, pageable)));
    }

    @Operation(summary = "Listar cultivos por productor")
    @GetMapping("/productor/{productorId}")
    public ResponseEntity<ApiResponse<Page<CultivoResponse>>> listarPorProductor(
            @PathVariable Long productorId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.listarPorProductor(productorId, pageable)));
    }

    @Operation(summary = "Listar cultivos por finca")
    @GetMapping("/finca/{fincaId}")
    public ResponseEntity<ApiResponse<Page<CultivoResponse>>> listarPorFinca(
            @PathVariable Long fincaId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.listarPorFinca(fincaId, pageable)));
    }

    @Operation(summary = "Listar cultivos por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<Page<CultivoResponse>>> listarPorEstado(
            @PathVariable String estado, @PageableDefault(size = 10) Pageable pageable) {
        EstadoCultivo ec = EstadoCultivo.valueOf(estado.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.listarPorEstado(ec, pageable)));
    }

    @Operation(summary = "Obtener cultivo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CultivoResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(cultivoService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear nuevo cultivo")
    @PostMapping
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CultivoResponse>> crear(@Valid @RequestBody CultivoRequest request) {
        CultivoResponse response = cultivoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Actualizar cultivo")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<CultivoResponse>> actualizar(
            @PathVariable Long id, @Valid @RequestBody CultivoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cultivo actualizado", cultivoService.actualizar(id, request)));
    }

    @Operation(summary = "Eliminar cultivo (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        cultivoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cultivo eliminado", null));
    }
}
