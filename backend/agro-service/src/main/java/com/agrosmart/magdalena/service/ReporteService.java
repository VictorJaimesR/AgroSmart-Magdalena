package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.entity.Reporte;
import com.agrosmart.magdalena.domain.enums.TipoReporte;
import com.agrosmart.magdalena.dto.request.ReporteRequest;
import com.agrosmart.magdalena.dto.response.ReporteResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ProductorRepository;
import com.agrosmart.magdalena.repository.ReporteRepository;
import com.agrosmart.magdalena.domain.entity.Cultivo;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio de generación y consulta de reportes de producción.
 * Genera un resumen en formato texto/JSON del estado productivo del productor.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ProductorRepository productorRepository;
    private final FincaRepository fincaRepository;
    private final CultivoRepository cultivoRepository;
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

    /**
     * Genera un reporte de producción para un productor.
     * El contenido se genera automáticamente basado en las fincas y cultivos del productor.
     */
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

        // Generar contenido del reporte
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
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"productor\": \"%s\", ", "Agricultor " + productor.getUsuarioId()));
        sb.append(String.format("\"cedula\": \"%s\", ", productor.getCedula()));
        sb.append(String.format("\"tipo_reporte\": \"%s\", ", tipo.name()));
        sb.append(String.format("\"total_fincas\": %d, ", totalFincas));
        sb.append(String.format("\"asociacion\": \"%s\"}",
                productor.getAsociacion() != null ? productor.getAsociacion().getNombre() : "Sin asociación"));
        return sb.toString();
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

    @Transactional(readOnly = true)
    public String generarCsvProduccion(Long usuarioId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Productor,Cultivo,Area_Utilizada,Rendimiento_Estimado,Estado\n");
        // Simplified query logic for quick export
        Iterable<Cultivo> cultivos = usuarioId != null 
            ? cultivoRepository.findByProductorUsuarioId(usuarioId, Pageable.unpaged()) 
            : cultivoRepository.findAll();
        for (Cultivo c : cultivos) {
            String prodNombre = "Agricultor ID: " + c.getParcela().getFinca().getProductor().getUsuarioId();
            csv.append(String.format("%s,%s,%.2f,%.2f,%s\n", 
                prodNombre, c.getNombre(), c.getAreaUtilizada(), c.getRendimientoEsperado(), c.getEstado().name()
            ));
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String generarCsvInventarioCultivos(Long fincaId) {
        StringBuilder csv = new StringBuilder();
        csv.append("Finca,Parcela,Cultivo,Variedad,Fecha_Siembra,Estado\n");
        Iterable<Cultivo> cultivos = fincaId != null 
            ? cultivoRepository.findByFincaId(fincaId, Pageable.unpaged()) 
            : cultivoRepository.findAll();
        for (Cultivo c : cultivos) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s\n", 
                c.getParcela().getFinca().getNombre(),
                c.getParcela().getNombre(),
                c.getNombre(),
                c.getVariedad(),
                c.getFechaSiembra() != null ? c.getFechaSiembra().toString() : "N/A",
                c.getEstado().name()
            ));
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String generarCsvAlertas() {
        StringBuilder csv = new StringBuilder();
        csv.append("Tipo_Alerta,Titulo,Fecha_Emision,Estado,Activa\n");
        Map<String, Object> response = restTemplate.getForObject("http://localhost:8083/api/alertas", Map.class);
        if (response != null && response.get("datos") instanceof Map<?, ?> datos && datos.get("content") instanceof List<?> alertas) {
            for (Object item : alertas) {
                if (item instanceof Map<?, ?> alerta) {
                    csv.append(String.format("%s,%s,%s,%s,%s\n",
                            safe(alerta.get("tipo")),
                            safe(alerta.get("titulo")),
                            safe(alerta.get("fechaEmision")),
                            safe(alerta.get("estadoAlerta")),
                            safe(alerta.get("activa"))));
                }
            }
        }
        return csv.toString();
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "N/A";
    }
}
