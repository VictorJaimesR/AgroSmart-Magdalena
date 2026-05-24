package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Cultivo;
import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.entity.Reporte;
import com.agrosmart.magdalena.domain.enums.TipoReporte;
import com.agrosmart.magdalena.dto.request.ReporteRequest;
import com.agrosmart.magdalena.dto.response.ReporteResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.ActividadRepository;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ProductorRepository;
import com.agrosmart.magdalena.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ProductorRepository productorRepository;
    private final FincaRepository fincaRepository;
    private final CultivoRepository cultivoRepository;
    private final ActividadRepository actividadRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public Page<ReporteResponse> listarPorProductor(Long usuarioId, Pageable pageable) {
        return reporteRepository.findByProductorUsuarioId(usuarioId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtenerPorId(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", id));
        return toResponse(reporte);
    }

    @Transactional
    public ReporteResponse generar(Long usuarioId, ReporteRequest request) {
        Productor productor = productorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Productor", "usuarioId", usuarioId));

        TipoReporte tipo;
        try {
            tipo = TipoReporte.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de reporte inválido: " + request.getTipo());
        }

        long totalFincas = fincaRepository.findByProductorUsuarioId(usuarioId, Pageable.unpaged()).getTotalElements();
        String contenido = generarContenidoReporte(productor, tipo, totalFincas);

        Reporte reporte = Reporte.builder()
                .productor(productor)
                .tipo(tipo)
                .titulo(request.getTitulo() != null ? request.getTitulo()
                        : "Reporte de " + tipo.name().toLowerCase() + " - Agricultor ID: " + productor.getUsuarioId())
                .contenido(contenido)
                .periodoInicio(request.getPeriodoInicio())
                .periodoFin(request.getPeriodoFin())
                .build();

        reporte = reporteRepository.save(reporte);
        return toResponse(reporte);
    }

    private String generarContenidoReporte(Productor productor, TipoReporte tipo, long totalFincas) {
        return String.format(
                "{\"productor\": \"Agricultor ID: %d\", \"cedula\": \"%s\", \"tipo_reporte\": \"%s\", \"total_fincas\": %d, \"asociacion\": \"%s\"}",
                productor.getUsuarioId(),
                productor.getCedula(),
                tipo.name(),
                totalFincas,
                productor.getAsociacion() != null ? productor.getAsociacion().getNombre() : "Sin asociación"
        );
    }

    private ReporteResponse toResponse(Reporte r) {
        return ReporteResponse.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .tipo(r.getTipo().name())
                .contenido(r.getContenido())
                .periodoInicio(r.getPeriodoInicio())
                .periodoFin(r.getPeriodoFin())
                .fechaGeneracion(r.getFechaGeneracion())
                .createdAt(r.getCreatedAt())
                .productorId(r.getProductor().getId())
                .productorNombre("Agricultor ID: " + r.getProductor().getUsuarioId())
                .build();
    }

    // ── CSV Producción ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public String generarCsvProduccion(Long usuarioId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Productor,Cultivo,Variedad,Area_Utilizada_ha,Rendimiento_Esperado_ton_ha,Estado,Fecha_Siembra,Fecha_Cosecha_Estimada\n");

        Iterable<Cultivo> cultivos = usuarioId != null
                ? cultivoRepository.findByProductorUsuarioId(usuarioId, Pageable.unpaged())
                : cultivoRepository.findAll();

        for (Cultivo c : cultivos) {
            String productor = "Agricultor ID: " + c.getParcela().getFinca().getProductor().getUsuarioId();
            String variedad  = c.getVariedad() != null ? c.getVariedad() : "N/A";
            String area      = c.getAreaUtilizada() != null
                    ? String.format(Locale.US, "%.2f", c.getAreaUtilizada()) : "0.00";
            String rendimiento = c.getRendimientoEsperado() != null
                    ? String.format(Locale.US, "%.2f", c.getRendimientoEsperado()) : "N/A";
            String fechaSiembra = c.getFechaSiembra() != null ? c.getFechaSiembra().toString() : "N/A";
            String fechaCosecha = c.getFechaCosechaEstimada() != null ? c.getFechaCosechaEstimada().toString() : "N/A";

            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    productor, c.getNombre(), variedad, area,
                    rendimiento, c.getEstado().name(), fechaSiembra, fechaCosecha));
        }
        return csv.toString();
    }

    // ── CSV Inventario de insumos (basado en actividades) ─────────────────────

    @Transactional(readOnly = true)
    public String generarCsvInventarioCultivos(Long usuarioId) {
        StringBuilder csv = new StringBuilder();

        // ── Sección 1: Detalle de actividades ──
        csv.append("=== DETALLE DE ACTIVIDADES AGRICOLAS ===\n");
        csv.append("Finca,Cultivo,Tipo_Actividad,Producto_Insumo,Cantidad,Unidad,Fecha,Registrado_Por\n");

        List<Object[]> actividades = actividadRepository.findByProductorUsuarioId(usuarioId)
                .stream()
                .map(a -> new Object[]{
                        a.getFinca().getNombre(),
                        a.getCultivo().getNombre(),
                        a.getTipoActividad().name(),
                        a.getProducto() != null ? a.getProducto() : "N/A",
                        a.getCantidad() != null ? String.format(Locale.US, "%.2f", a.getCantidad()) : "N/A",
                        a.getUnidad() != null ? a.getUnidad() : "N/A",
                        a.getFechaActividad() != null ? a.getFechaActividad().toLocalDate().toString() : "N/A",
                        a.getRegistradoPorNombre()
                }).toList();

        for (Object[] row : actividades) {
            csv.append(String.format("%s,%s,%s,\"%s\",%s,%s,%s,%s\n",
                    row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7]));
        }

        // ── Sección 2: Resumen de insumos con totales ──
        csv.append("\n=== RESUMEN DE INSUMOS UTILIZADOS ===\n");
        csv.append("Tipo_Actividad,Finca,Cultivo,Producto_Insumo,Unidad,Total_Utilizado,Nro_Aplicaciones\n");

        List<Object[]> resumenInsumos = actividadRepository.resumenInsumosPorProductor(usuarioId);
        for (Object[] row : resumenInsumos) {
            // row: tipoActividad, producto, unidad, SUM(cantidad), COUNT, finca, cultivo
            String total = row[3] != null ? String.format(Locale.US, "%.2f", ((Number) row[3]).doubleValue()) : "0.00";
            csv.append(String.format("%s,%s,%s,\"%s\",%s,%s,%s\n",
                    row[0], row[5], row[6],
                    row[1] != null ? row[1] : "N/A",
                    row[2] != null ? row[2] : "N/A",
                    total, row[4]));
        }

        // ── Sección 3: Resumen de actividades sin insumo (riego, poda, etc.) ──
        csv.append("\n=== RESUMEN DE ACTIVIDADES POR TIPO ===\n");
        csv.append("Tipo_Actividad,Finca,Cultivo,Nro_Veces,Total_Cantidad,Unidad\n");

        List<Object[]> resumenActividades = actividadRepository.resumenActividadesPorProductor(usuarioId);
        for (Object[] row : resumenActividades) {
            // row: tipoActividad, finca, cultivo, COUNT, SUM(cantidad), unidad
            String total = row[4] != null ? String.format(Locale.US, "%.2f", ((Number) row[4]).doubleValue()) : "0.00";
            csv.append(String.format("%s,%s,%s,%s,%s,%s\n",
                    row[0], row[1], row[2], row[3], total,
                    row[5] != null ? row[5] : "N/A"));
        }

        return csv.toString();
    }

    // ── CSV Alertas ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public String generarCsvAlertas() {
        StringBuilder csv = new StringBuilder();
        csv.append("Tipo_Alerta,Titulo,Fecha_Emision,Estado,Activa,Municipios_Afectados\n");

        try {
            Map<String, Object> response = restTemplate.getForObject(
                    "http://localhost:8083/api/alertas?size=100", Map.class);

            if (response != null && response.get("datos") instanceof Map<?, ?> datos
                    && datos.get("content") instanceof List<?> alertas) {
                for (Object item : alertas) {
                    if (item instanceof Map<?, ?> alerta) {
                        String municipios = "";
                        if (alerta.get("municipiosAfectados") instanceof List<?> lista) {
                            municipios = "\"" + String.join(", ", lista.stream()
                                    .map(Object::toString).toList()) + "\"";
                        }
                        csv.append(String.format("%s,\"%s\",%s,%s,%s,%s\n",
                                safe(alerta.get("tipo")),
                                safe(alerta.get("titulo")).replace("\"", "\"\""),
                                safe(alerta.get("fechaEmision")),
                                safe(alerta.get("estadoAlerta")),
                                safe(alerta.get("activa")),
                                municipios));
                    }
                }
            }
        } catch (Exception e) {
            csv.append("Error al obtener alertas: ").append(e.getMessage()).append("\n");
        }
        return csv.toString();
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "N/A";
    }
}