package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.request.ParcelaRequest;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.ParcelaResponse;
import com.agrosmart.magdalena.service.ParcelaService;
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

@Tag(name = "Parcelas", description = "Gestión de parcelas/lotes dentro de fincas")
@RestController
@RequestMapping("/parcelas")
@RequiredArgsConstructor
public class ParcelaController {

    private final ParcelaService parcelaService;

    @Operation(summary = "Listar parcelas por finca")
    @GetMapping("/finca/{fincaId}")
    public ResponseEntity<ApiResponse<Page<ParcelaResponse>>> listarPorFinca(
            @PathVariable Long fincaId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(parcelaService.listarPorFinca(fincaId, pageable)));
    }

    @Operation(summary = "Obtener parcela por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParcelaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(parcelaService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear nueva parcela")
    @PostMapping
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ParcelaResponse>> crear(@Valid @RequestBody ParcelaRequest request) {
        ParcelaResponse response = parcelaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Actualizar parcela")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ParcelaResponse>> actualizar(
            @PathVariable Long id, @Valid @RequestBody ParcelaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Parcela actualizada", parcelaService.actualizar(id, request)));
    }

    @Operation(summary = "Eliminar parcela (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGRICULTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        parcelaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Parcela eliminada", null));
    }
}
