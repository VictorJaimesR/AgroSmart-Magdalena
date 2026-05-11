package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.request.ParametroTecnicoRequest;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.ParametroTecnicoResponse;
import com.agrosmart.magdalena.service.ParametroTecnicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parámetros Técnicos", description = "Configuración de parámetros del sistema (solo admin)")
@RestController
@RequestMapping("/parametros-tecnicos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ParametroTecnicoController {

    private final ParametroTecnicoService parametroService;

    @Operation(summary = "Listar todos los parámetros")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ParametroTecnicoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(parametroService.listarTodos()));
    }

    @Operation(summary = "Listar parámetros por categoría")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<ParametroTecnicoResponse>>> listarPorCategoria(
            @PathVariable String categoria) {
        return ResponseEntity.ok(ApiResponse.ok(parametroService.listarPorCategoria(categoria)));
    }

    @Operation(summary = "Obtener parámetro por clave")
    @GetMapping("/{clave}")
    public ResponseEntity<ApiResponse<ParametroTecnicoResponse>> obtener(@PathVariable String clave) {
        return ResponseEntity.ok(ApiResponse.ok(parametroService.obtenerPorClave(clave)));
    }

    @Operation(summary = "Crear parámetro técnico")
    @PostMapping
    public ResponseEntity<ApiResponse<ParametroTecnicoResponse>> crear(
            @Valid @RequestBody ParametroTecnicoRequest request, Authentication auth) {
        ParametroTecnicoResponse response = parametroService.crear(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Actualizar parámetro técnico")
    @PutMapping("/{clave}")
    public ResponseEntity<ApiResponse<ParametroTecnicoResponse>> actualizar(
            @PathVariable String clave, @Valid @RequestBody ParametroTecnicoRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Parámetro actualizado",
                parametroService.actualizar(clave, request, auth.getName())));
    }

    @Operation(summary = "Eliminar parámetro técnico")
    @DeleteMapping("/{clave}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String clave) {
        parametroService.eliminar(clave);
        return ResponseEntity.ok(ApiResponse.ok("Parámetro eliminado", null));
    }
}
