package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.ActividadAgricola;
import com.agrosmart.magdalena.domain.entity.Cultivo;
import com.agrosmart.magdalena.domain.entity.Finca;
import com.agrosmart.magdalena.domain.enums.TipoActividad;
import com.agrosmart.magdalena.dto.request.ActividadRequest;
import com.agrosmart.magdalena.dto.response.ActividadResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.ActividadRepository;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.FincaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para el registro y consulta de actividades agrícolas.
 *
 * Reglas de negocio:
 *   - El cultivo debe pertenecer (a través de su parcela) a la finca indicada
 *   - La actividad queda asociada al usuario (agricultor o técnico) que la registra
 *   - El historial es inmutable: no se editan ni eliminan actividades ya registradas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final FincaRepository fincaRepository;
    private final CultivoRepository cultivoRepository;

    /**
     * Registra una nueva actividad agrícola sobre un cultivo.
     *
     * @param request       datos de la actividad
     * @param userId        ID del usuario que registra
     * @param userName      nombre completo del usuario
     * @param userRole      rol del usuario (AGRICULTOR / TECNICO)
     */
    @Transactional
    public ActividadResponse registrar(ActividadRequest request, Long userId, String userName, String userRole) {
        Finca finca = fincaRepository.findById(request.getFincaId())
                .orElseThrow(() -> new ResourceNotFoundException("Finca", "id", request.getFincaId()));

        if (!finca.getActivo()) {
            throw new BadRequestException("La finca no está activa");
        }

        Cultivo cultivo = cultivoRepository.findById(request.getCultivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", "id", request.getCultivoId()));

        if (!cultivo.getActivo()) {
            throw new BadRequestException("El cultivo no está activo");
        }

        // Validar que el cultivo pertenezca a la finca indicada
        Long fincaDelCultivo = cultivo.getParcela().getFinca().getId();
        if (!fincaDelCultivo.equals(request.getFincaId())) {
            throw new BadRequestException("El cultivo no pertenece a la finca indicada");
        }

        TipoActividad tipo;
        try {
            tipo = TipoActividad.valueOf(request.getTipoActividad().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de actividad inválido: " + request.getTipoActividad());
        }

        ActividadAgricola actividad = ActividadAgricola.builder()
                .finca(finca)
                .cultivo(cultivo)
                .tipoActividad(tipo)
                .cantidad(request.getCantidad())
                .unidad(request.getUnidad())
                .producto(request.getProducto())
                .observaciones(request.getObservaciones())
                .registradoPorId(userId)
                .registradoPorNombre(userName)
                .registradoPorRol(userRole)
                .fechaActividad(request.getFechaActividad())
                .build();

        ActividadAgricola saved = actividadRepository.save(actividad);
        log.info("Actividad {} registrada en cultivo {} de finca {} por usuario {}",
                tipo, cultivo.getId(), finca.getId(), userId);

        return toResponse(saved);
    }

    /** Historial completo de una finca (todas las actividades de todos sus cultivos) */
    @Transactional(readOnly = true)
    public Page<ActividadResponse> listarPorFinca(Long fincaId, Pageable pageable) {
        return actividadRepository.findByFincaIdOrderByFechaActividadDesc(fincaId, pageable)
                .map(this::toResponse);
    }

    /** Historial de un cultivo específico */
    @Transactional(readOnly = true)
    public Page<ActividadResponse> listarPorCultivo(Long cultivoId, Pageable pageable) {
        return actividadRepository.findByCultivoIdOrderByFechaActividadDesc(cultivoId, pageable)
                .map(this::toResponse);
    }

    /** Historial de una finca filtrado por tipo de actividad */
    @Transactional(readOnly = true)
    public Page<ActividadResponse> listarPorFincaYTipo(Long fincaId, String tipo, Pageable pageable) {
        TipoActividad tipoActividad;
        try {
            tipoActividad = TipoActividad.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de actividad inválido: " + tipo);
        }
        return actividadRepository.findByFincaIdAndTipoActividadOrderByFechaActividadDesc(fincaId, tipoActividad, pageable)
                .map(this::toResponse);
    }

    /** Resumen de actividades por tipo para una finca (útil para dashboard) */
    @Transactional(readOnly = true)
    public Map<String, Long> resumenPorFinca(Long fincaId) {
        List<Object[]> rows = actividadRepository.contarPorTipoEnFinca(fincaId);
        Map<String, Long> resumen = new HashMap<>();
        for (Object[] row : rows) {
            TipoActividad tipo = (TipoActividad) row[0];
            Long count = (Long) row[1];
            resumen.put(tipo.name(), count);
        }
        return resumen;
    }

    private ActividadResponse toResponse(ActividadAgricola a) {
        return ActividadResponse.builder()
                .id(a.getId())
                .fincaId(a.getFinca().getId())
                .fincaNombre(a.getFinca().getNombre())
                .cultivoId(a.getCultivo().getId())
                .cultivoNombre(a.getCultivo().getNombre())
                .cultivoVariedad(a.getCultivo().getVariedad())
                .tipoActividad(a.getTipoActividad().name())
                .cantidad(a.getCantidad())
                .unidad(a.getUnidad())
                .producto(a.getProducto())
                .observaciones(a.getObservaciones())
                .registradoPorId(a.getRegistradoPorId())
                .registradoPorNombre(a.getRegistradoPorNombre())
                .registradoPorRol(a.getRegistradoPorRol())
                .fechaActividad(a.getFechaActividad())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
