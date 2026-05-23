package com.campovital.recommendations.service;

import com.campovital.recommendations.domain.entity.Cultivo;
import com.campovital.recommendations.domain.entity.Finca;
import com.campovital.recommendations.domain.entity.HistorialRecomendacion;
import com.campovital.recommendations.domain.entity.Parcela;
import com.campovital.recommendations.domain.entity.Productor;
import com.campovital.recommendations.domain.entity.Recomendacion;
import com.campovital.recommendations.domain.entity.TecnicoAgropecuario;
import com.campovital.recommendations.domain.enums.EstadoCultivo;
import com.campovital.recommendations.domain.enums.Prioridad;
import com.campovital.recommendations.dto.request.RecomendacionRequest;
import com.campovital.recommendations.dto.response.RecomendacionResponse;
import com.campovital.recommendations.exception.BadRequestException;
import com.campovital.recommendations.exception.ResourceNotFoundException;
import com.campovital.recommendations.repository.CultivoRepository;
import com.campovital.recommendations.repository.HistorialRecomendacionRepository;
import com.campovital.recommendations.repository.RecomendacionRepository;
import com.campovital.recommendations.repository.TecnicoAgropecuarioRepository;
import com.campovital.recommendations.repository.UsuarioRepository;
import com.campovital.recommendations.repository.FincaRepository;
import com.campovital.recommendations.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private final RecomendacionRepository recomendacionRepository;
    private final CultivoRepository cultivoRepository;
    private final HistorialRecomendacionRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoAgropecuarioRepository tecnicoRepository;
    private final RestTemplate restTemplate;
    private final FincaRepository fincaRepository;
    private final ParcelaRepository parcelaRepository;

    @Value("${app.agro-service.url:http://localhost:8082}")
    private String agroServiceUrl;

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RecomendacionResponse> listarPorCultivo(Long cultivoId, Pageable pageable) {
        return recomendacionRepository.findByCultivoId(cultivoId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RecomendacionResponse> listarPorCultivoIds(List<Long> cultivoIds, Pageable pageable) {
        if (cultivoIds == null || cultivoIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return recomendacionRepository.findByCultivoAgroIdIn(cultivoIds, pageable)
                .map(r -> toResponse(r));
    }

    @Transactional(readOnly = true)
    public Page<RecomendacionResponse> listarPorProductor(Long productorId, Pageable pageable) {
        return recomendacionRepository.findByProductorId(productorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RecomendacionResponse> listarPendientes(Pageable pageable) {
        return recomendacionRepository.findByAplicadaFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RecomendacionResponse obtenerPorId(Long id) {
        Recomendacion r = recomendacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacion", "id", id));
        return toResponse(r);
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Transactional
    public RecomendacionResponse crear(RecomendacionRequest request) {
        // Buscar por agroId (el ID que viene del frontend = ID en agro-service)
        Cultivo cultivo = cultivoRepository.findByAgroId(request.getCultivoId())
                .orElseGet(() -> sincronizarCultivo(request.getCultivoId()));

        Prioridad prioridad = Prioridad.MEDIA;
        if (request.getPrioridad() != null) {
            try {
                prioridad = Prioridad.valueOf(request.getPrioridad().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Prioridad inválida: " + request.getPrioridad());
            }
        }

        Recomendacion recomendacion = Recomendacion.builder()
                .cultivo(cultivo)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .prioridad(prioridad)
                .build();

        recomendacion = recomendacionRepository.save(recomendacion);
        return toResponse(recomendacion);
    }

    // ── Marcar aplicada ───────────────────────────────────────────────────────

    @Transactional
    public RecomendacionResponse marcarComoAplicada(Long id, String observaciones) {
        Recomendacion r = recomendacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacion", "id", id));

        r.setAplicada(true);
        r = recomendacionRepository.save(r);

        HistorialRecomendacion historial = HistorialRecomendacion.builder()
                .recomendacion(r)
                .fechaRegistro(LocalDateTime.now())
                .observaciones(observaciones)
                .aplicada(true)
                .build();
        historialRepository.save(historial);

        return toResponse(r);
    }

    // ── Complementar ──────────────────────────────────────────────────────────

    @Transactional
    public RecomendacionResponse complementar(Long id, String notaComplementaria, String emailTecnico) {
        Recomendacion r = recomendacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendacion", "id", id));

        var usuario = usuarioRepository.findByEmail(emailTecnico)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailTecnico));

        TecnicoAgropecuario tecnico = tecnicoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new BadRequestException("El usuario no es un técnico agropecuario"));

        HistorialRecomendacion historial = HistorialRecomendacion.builder()
                .recomendacion(r)
                .fechaRegistro(LocalDateTime.now())
                .observaciones("COMPLEMENTO TÉCNICO: " + notaComplementaria)
                .aplicada(r.getAplicada())
                .build();
        historialRepository.save(historial);

        if (r.getTecnico() == null) {
            r.setTecnico(tecnico);
            recomendacionRepository.save(r);
        }

        return toResponse(r);
    }

    // ── Sincronización automática ─────────────────────────────────────────────

    /**
     * Consulta el agro-service para obtener los datos del cultivo
     * y lo guarda localmente con su agroId para futuras referencias.
     */
    @Transactional
    public Cultivo sincronizarCultivo(Long agroId) {
        log.info("Sincronizando cultivo agroId={} desde agro-service...", agroId);
        try {
            String url = agroServiceUrl + "/api/cultivos/" + agroId;

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-User-Role", "TECNICO");
            headers.set("X-User-Email", "internal@system.local");
            headers.set("X-User-Id", "0");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> responseEntity = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, entity, Map.class);

            Map response = responseEntity.getBody();
            if (response == null)
                throw new ResourceNotFoundException("Cultivo", "id", agroId);

            Map<String, Object> datos = (Map<String, Object>) response.get("datos");
            if (datos == null)
                datos = response;

            String nombreCultivo = (String) datos.getOrDefault("nombre", "Cultivo " + agroId);
            String estadoStr = (String) datos.getOrDefault("estado", "PLANIFICADO");
            String parcelaNombre = (String) datos.getOrDefault("parcelaNombre", "Parcela");
            String fincaNombre = (String) datos.getOrDefault("fincaNombre", "Finca");

            EstadoCultivo estado;
            try {
                estado = EstadoCultivo.valueOf(estadoStr);
            } catch (Exception e) {
                estado = EstadoCultivo.PLANIFICADO;
            }

            // 1. Guardar Finca primero
            Finca finca = new Finca();
            finca.setNombre(fincaNombre);
            finca.setActivo(true);
            finca = fincaRepository.save(finca);

            // 2. Guardar Parcela con la Finca ya persistida
            Parcela parcela = new Parcela();
            parcela.setNombre(parcelaNombre);
            parcela.setFinca(finca);
            parcela.setActivo(true);
            parcela = parcelaRepository.save(parcela);

            // 3. Guardar Cultivo con la Parcela ya persistida
            Cultivo cultivo = new Cultivo();
            cultivo.setNombre(nombreCultivo);
            cultivo.setParcela(parcela);
            cultivo.setEstado(estado);
            cultivo.setActivo(true);
            cultivo.setAgroId(agroId);
            cultivo = cultivoRepository.save(cultivo);

            log.info("Cultivo agroId={} sincronizado con id local={}", agroId, cultivo.getId());
            return cultivo;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al sincronizar cultivo agroId={}: {}", agroId, e.getMessage());
            throw new ResourceNotFoundException("Cultivo", "agroId", agroId);
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private RecomendacionResponse toResponse(Recomendacion r) {
        TecnicoAgropecuario tecnico = r.getTecnico();
        // Devolver el agroId al frontend para que coincida con lo que conoce
        Long cultivoIdParaFrontend = r.getCultivo().getAgroId() != null
                ? r.getCultivo().getAgroId()
                : r.getCultivo().getId();
        return RecomendacionResponse.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .prioridad(r.getPrioridad().name())
                .aplicada(r.getAplicada())
                .fechaEmision(r.getFechaEmision())
                .createdAt(r.getCreatedAt())
                .cultivoId(cultivoIdParaFrontend)
                .cultivoNombre(r.getCultivo().getNombre())
                .tecnicoId(tecnico != null ? tecnico.getId() : null)
                .tecnicoNombre(tecnico != null ? "Técnico ID: " + tecnico.getUsuarioId() : null)
                .build();
    }
}