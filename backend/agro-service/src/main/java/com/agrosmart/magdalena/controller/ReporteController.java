package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.dto.request.ReporteRequest;
import com.agrosmart.magdalena.dto.response.ApiResponse;
import com.agrosmart.magdalena.dto.response.ReporteResponse;
import com.agrosmart.magdalena.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reportes", description = "Generación y consulta de reportes")
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Listar reportes por productor")
    @GetMapping("/productor/{productorId}")
    public ResponseEntity<ApiResponse<Page<ReporteResponse>>> listarPorProductor(
            @PathVariable Long productorId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.listarPorProductor(productorId, pageable)));
    }

    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReporteResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.obtenerPorId(id)));
    }

    @Operation(summary = "Generar nuevo reporte de producción")
    @PostMapping("/productor/{productorId}")
    public ResponseEntity<ApiResponse<ReporteResponse>> generar(
            @PathVariable Long productorId, @Valid @RequestBody ReporteRequest request) {
        ReporteResponse response = reporteService.generar(productorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Exportar Producción en CSV")
    @GetMapping(value = "/produccion-csv", produces = "text/csv")
    public ResponseEntity<String> exportarProduccionCsv(@RequestParam(required = false) Long productorId) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"produccion.csv\"")
                .body(reporteService.generarCsvProduccion(productorId));
    }

    @Operation(summary = "Exportar Inventario de Cultivos en CSV")
    @GetMapping(value = "/inventario-cultivos-csv", produces = "text/csv")
    public ResponseEntity<String> exportarInventarioCsv(@RequestParam(required = false) Long fincaId) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"inventario_cultivos.csv\"")
                .body(reporteService.generarCsvInventarioCultivos(fincaId));
    }

    @Operation(summary = "Exportar Historial de Alertas en CSV")
    @GetMapping(value = "/alertas-historial-csv", produces = "text/csv")
    public ResponseEntity<String> exportarAlertasCsv() {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"alertas_historial.csv\"")
                .body(reporteService.generarCsvAlertas());
    }
}
