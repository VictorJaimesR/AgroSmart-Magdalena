package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.AdminFincaResponse;
import com.agrosmart.magdalena.dto.response.AdminFincaDetalleResponse;
import com.agrosmart.magdalena.service.AdminFincaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador exclusivo para vistas ADMIN de fincas.
 * Proporciona endpoints de lectura global sin capacidad de modificación.
 *
 * Endpoints:
 *   - GET /admin/fincas - Listado global de fincas activas (paginado)
 *   - GET /admin/fincas/{id} - Detalle completo de finca con parcelas y cultivos
 *
 * NO modifica endpoints existentes (/fincas, /fincas/{id}, etc.) usados por PRODUCTOR.
 */
@Tag(name = "Admin Fincas", description = "Gestión global de fincas para administradores (solo lectura)")
@RestController
@RequestMapping("/admin/fincas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFincaController {

    private final AdminFincaService adminFincaService;

    /**
     * Listado global de fincas activas del sistema.
     * Solo lectura, sin capacidad de crear, editar o eliminar.
     *
     * @param pageable Parámetros de paginación (tamaño, página, ordenamiento)
     * @return Page<AdminFincaResponse> con información agregada de fincas
     */
    @Operation(summary = "Listar todas las fincas activas del sistema (ADMIN)",
            description = "Retorna un listado paginado de todas las fincas activas con información agregada. Solo lectura.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminFincaResponse>>> listarFincas(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<AdminFincaResponse> fincas = adminFincaService.listarFincasActivas(pageable);
        return ResponseEntity.ok(ApiResponse.ok(fincas));
    }

    /**
     * Obtiene el detalle completo de una finca incluyendo:
     * - Información general
     * - Productor (agricultor dueño)
     * - Técnico asociado
     * - Parcelas con cultivos
     * - Hectáreas de cada cultivo
     * - Fechas y estado
     * - Rendimiento
     *
     * @param id Identificador de la finca
     * @return AdminFincaDetalleResponse con información completa
     */
    @Operation(summary = "Obtener detalle completo de una finca (ADMIN)",
            description = "Retorna información detallada de la finca incluyendo parcelas, cultivos, técnico asociado y métricas. Solo lectura.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminFincaDetalleResponse>> obtenerDetalle(
            @PathVariable Long id) {
        AdminFincaDetalleResponse detalle = adminFincaService.obtenerDetalleCompleto(id);
        return ResponseEntity.ok(ApiResponse.ok(detalle));
    }
}
