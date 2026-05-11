package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Cultivo;
import com.agrosmart.magdalena.domain.entity.Parcela;
import com.agrosmart.magdalena.domain.enums.EstadoCultivo;
import com.agrosmart.magdalena.domain.enums.EstadoParcela;
import com.agrosmart.magdalena.dto.request.CultivoRequest;
import com.agrosmart.magdalena.dto.response.CultivoResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agrosmart.magdalena.domain.event.CultivoStateChangedEvent;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio de gestión de cultivos.
 *
 * Reglas de negocio:
 *   - Un cultivo pertenece a una parcela
 *   - La parcela debe estar DISPONIBLE para crear un cultivo activo
 *   - Al crear/actualizar cultivo a estado activo, la parcela se marca OCUPADA
 *   - Al finalizar/cosechar/cancelar cultivo, la parcela se marca DISPONIBLE
 *   - El área utilizada no debe exceder el área de la parcela
 *   - La fecha de siembra no puede ser posterior a la fecha estimada de cosecha
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CultivoService {

    private final CultivoRepository cultivoRepository;
    private final ParcelaRepository parcelaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public Page<CultivoResponse> listarTodos(Pageable pageable) {
        return cultivoRepository.findByActivoTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CultivoResponse> listarPorParcela(Long parcelaId, Pageable pageable) {
        return cultivoRepository.findByParcelaIdAndActivoTrue(parcelaId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CultivoResponse> listarPorProductor(Long productorId, Pageable pageable) {
        return cultivoRepository.findByProductorId(productorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CultivoResponse> listarPorFinca(Long fincaId, Pageable pageable) {
        return cultivoRepository.findByFincaId(fincaId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CultivoResponse> listarPorEstado(EstadoCultivo estado, Pageable pageable) {
        return cultivoRepository.findByEstadoAndActivoTrue(estado, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CultivoResponse obtenerPorId(Long id) {
        Cultivo cultivo = buscarCultivoActivo(id);
        return toResponse(cultivo);
    }

    @Transactional
    public CultivoResponse crear(CultivoRequest request) {
        Parcela parcela = parcelaRepository.findById(request.getParcelaId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", "id", request.getParcelaId()));

        // Validar que la parcela exista y pertenezca a una finca activa
        if (!parcela.getActivo()) {
            throw new BadRequestException("La parcela no está activa");
        }

        // Validar que el área del cultivo sea EXACTAMENTE igual al área de la parcela
        if (request.getAreaUtilizada() != null) {
            Double diferencia = Math.abs(request.getAreaUtilizada() - parcela.getAreaParcela());
            if (diferencia > 0.0001) {
                throw new BadRequestException(String.format(
                        "El área del cultivo debe ser igual al área de la parcela (%.2f %s). Ingresa exactamente %.2f",
                        parcela.getAreaParcela(), parcela.getUnidadArea(), parcela.getAreaParcela()));
            }
        }

        // Validar fechas
        if (request.getFechaCosechaEstimada() != null
                && request.getFechaSiembra().isAfter(request.getFechaCosechaEstimada())) {
            throw new BadRequestException("La fecha de siembra no puede ser posterior a la fecha estimada de cosecha");
        }

        // Determinar estado
        EstadoCultivo estado = EstadoCultivo.PLANIFICADO;
        if (request.getEstado() != null) {
            try {
                estado = EstadoCultivo.valueOf(request.getEstado().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Estado de cultivo inválido: " + request.getEstado());
            }
        }

        // Si el estado es activo, validar que la parcela esté DISPONIBLE
        if (isEstadoActivo(estado)) {
            if (!EstadoParcela.DISPONIBLE.equals(parcela.getEstado())) {
                throw new BadRequestException("La parcela no está disponible. Estado actual: " + parcela.getEstado());
            }
        }

        Cultivo cultivo = Cultivo.builder()
                .nombre(request.getNombre())
                .variedad(request.getVariedad())
                .parcela(parcela)
                .fechaSiembra(request.getFechaSiembra())
                .fechaCosechaEstimada(request.getFechaCosechaEstimada())
                .estado(estado)
                .areaUtilizada(request.getAreaUtilizada())
                .observaciones(request.getObservaciones())
                .rendimientoEsperado(request.getRendimientoEsperado())
                .unidadRendimiento(request.getUnidadRendimiento() != null
                        ? request.getUnidadRendimiento() : "toneladas/hectárea")
                .imagenUrl(request.getImagenUrl())
                .build();

        cultivo = cultivoRepository.save(cultivo);

        // Actualizar estado de la parcela si el cultivo está activo
        if (isEstadoActivo(estado)) {
            parcela.setEstado(EstadoParcela.OCUPADA);
            parcelaRepository.save(parcela);
        }

        return toResponse(cultivo);
    }

    @Transactional
    public CultivoResponse actualizar(Long id, CultivoRequest request) {
        Cultivo cultivo = buscarCultivoActivo(id);

        if (request.getAreaUtilizada() != null) {
            validarAreaDisponible(cultivo.getParcela().getId(), request.getAreaUtilizada(), id);
        }

        if (request.getFechaCosechaEstimada() != null
                && request.getFechaSiembra().isAfter(request.getFechaCosechaEstimada())) {
            throw new BadRequestException("La fecha de siembra no puede ser posterior a la fecha estimada de cosecha");
        }

        cultivo.setNombre(request.getNombre());
        cultivo.setVariedad(request.getVariedad());
        cultivo.setFechaSiembra(request.getFechaSiembra());
        cultivo.setFechaCosechaEstimada(request.getFechaCosechaEstimada());
        cultivo.setAreaUtilizada(request.getAreaUtilizada());
        cultivo.setObservaciones(request.getObservaciones());
        cultivo.setRendimientoEsperado(request.getRendimientoEsperado());
        if (request.getUnidadRendimiento() != null) {
            cultivo.setUnidadRendimiento(request.getUnidadRendimiento());
        }
        if (request.getImagenUrl() != null) {
            cultivo.setImagenUrl(request.getImagenUrl());
        }
        if (request.getEstado() != null) {
            try {
                EstadoCultivo nuevoEstado = EstadoCultivo.valueOf(request.getEstado().toUpperCase());
                String estadoAnterior = cultivo.getEstado().name();
                if (cultivo.getEstado() != nuevoEstado) {
                    cultivo.setEstado(nuevoEstado);
                    
                    // Actualizar estado de la parcela según el nuevo estado del cultivo
                    Parcela parcela = cultivo.getParcela();
                    if (isEstadoActivo(nuevoEstado)) {
                        // Si el nuevo estado es activo, marcar parcela como OCUPADA
                        parcela.setEstado(EstadoParcela.OCUPADA);
                    } else if (isEstadoTerminal(nuevoEstado)) {
                        // Si el nuevo estado es terminal, marcar parcela como DISPONIBLE
                        parcela.setEstado(EstadoParcela.DISPONIBLE);
                    }
                    parcelaRepository.save(parcela);
                    
                    eventPublisher.publishEvent(new CultivoStateChangedEvent(cultivo, estadoAnterior, nuevoEstado.name()));
                    try {
                        restTemplate.postForEntity(
                                "http://localhost:8083/api/internal/cultivo-state-changed",
                                new HttpEntity<>(Map.of(
                                        "cultivoId", cultivo.getId(),
                                        "nuevoEstado", nuevoEstado.name()
                                )),
                                Void.class);
                    } catch (Exception ex) {
                        log.warn("No se pudo notificar a recommendations-service el cambio de estado del cultivo {}: {}",
                                cultivo.getId(), ex.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Estado de cultivo inválido: " + request.getEstado());
            }
        }

        cultivo = cultivoRepository.save(cultivo);
        return toResponse(cultivo);
    }

    @Transactional
    public void eliminar(Long id) {
        Cultivo cultivo = buscarCultivoActivo(id);
        
        // Obtener la parcela asociada al cultivo
        Parcela parcela = cultivo.getParcela();
        
        // Marcar cultivo como inactivo
        cultivo.setActivo(false);
        cultivoRepository.save(cultivo);
        
        // Reverter parcela a DISPONIBLE
        if (parcela != null && EstadoParcela.OCUPADA.equals(parcela.getEstado())) {
            parcela.setEstado(EstadoParcela.DISPONIBLE);
            parcelaRepository.save(parcela);
        }
    }

    /**
     * Valida que el área del nuevo cultivo no exceda el área de la parcela.
     */
    private void validarAreaDisponible(Long parcelaId, Double nuevaArea, Long excludeId) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", "id", parcelaId));

        Double areaUsada = cultivoRepository.sumAreaUtilizadaByParcelaId(parcelaId);
        if (areaUsada == null) areaUsada = 0.0;

        // Si es actualización, restar el área actual del cultivo
        if (excludeId != null) {
            Cultivo existente = cultivoRepository.findById(excludeId).orElse(null);
            if (existente != null && existente.getAreaUtilizada() != null) {
                areaUsada -= existente.getAreaUtilizada();
            }
        }

        if (areaUsada + nuevaArea > parcela.getAreaParcela()) {
            throw new BadRequestException(String.format(
                    "El área del cultivo (%.2f) excede el área disponible de la parcela (%.2f de %.2f %s)",
                    nuevaArea, parcela.getAreaParcela() - areaUsada, parcela.getAreaParcela(), parcela.getUnidadArea()));
        }
    }

    private Cultivo buscarCultivoActivo(Long id) {
        Cultivo cultivo = cultivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", "id", id));
        if (!cultivo.getActivo()) {
            throw new ResourceNotFoundException("Cultivo", "id", id);
        }
        return cultivo;
    }

    /**
     * Determina si un estado de cultivo es activo (mantiene parcela OCUPADA).
     */
    private boolean isEstadoActivo(EstadoCultivo estado) {
        return estado == EstadoCultivo.PLANIFICADO
                || estado == EstadoCultivo.SEMBRADO
                || estado == EstadoCultivo.EN_CRECIMIENTO
                || estado == EstadoCultivo.EN_COSECHA
                || estado == EstadoCultivo.ACTIVO;
    }

    /**
     * Determina si un estado de cultivo es terminal (libera parcela a DISPONIBLE).
     */
    private boolean isEstadoTerminal(EstadoCultivo estado) {
        return estado == EstadoCultivo.COSECHADO
                || estado == EstadoCultivo.FINALIZADO
                || estado == EstadoCultivo.CANCELADO
                || estado == EstadoCultivo.ABANDONADO;
    }

    private CultivoResponse toResponse(Cultivo c) {
        return CultivoResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .variedad(c.getVariedad())
                .fechaSiembra(c.getFechaSiembra())
                .fechaCosechaEstimada(c.getFechaCosechaEstimada())
                .fechaCosechaReal(c.getFechaCosechaReal())
                .estado(c.getEstado().name())
                .areaUtilizada(c.getAreaUtilizada())
                .observaciones(c.getObservaciones())
                .rendimientoEsperado(c.getRendimientoEsperado())
                .rendimientoReal(c.getRendimientoReal())
                .unidadRendimiento(c.getUnidadRendimiento())
                .activo(c.getActivo())
                .createdAt(c.getCreatedAt())
                .parcelaId(c.getParcela().getId())
                .parcelaNombre(c.getParcela().getNombre())
                .fincaId(c.getParcela().getFinca().getId())
                .fincaNombre(c.getParcela().getFinca().getNombre())
                .imagenUrl(c.getImagenUrl())
                .build();
    }
}
