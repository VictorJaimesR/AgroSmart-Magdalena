package com.campovital.recommendations.service;

import com.campovital.recommendations.domain.entity.Cultivo;
import com.campovital.recommendations.domain.entity.HistorialRecomendacion;
import com.campovital.recommendations.domain.entity.Recomendacion;
import com.campovital.recommendations.domain.entity.TecnicoAgropecuario;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecomendacionService {

    private final RecomendacionRepository recomendacionRepository;
    private final CultivoRepository cultivoRepository;
    private final HistorialRecomendacionRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoAgropecuarioRepository tecnicoRepository;

    @Transactional(readOnly = true)
    public Page<RecomendacionResponse> listarPorCultivo(Long cultivoId, Pageable pageable) {
        return recomendacionRepository.findByCultivoId(cultivoId, pageable).map(this::toResponse);
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

    @Transactional
    public RecomendacionResponse crear(RecomendacionRequest request) {
        Cultivo cultivo = cultivoRepository.findById(request.getCultivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", "id", request.getCultivoId()));

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

    private RecomendacionResponse toResponse(Recomendacion r) {
        TecnicoAgropecuario tecnico = r.getTecnico();
        return RecomendacionResponse.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .prioridad(r.getPrioridad().name())
                .aplicada(r.getAplicada())
                .fechaEmision(r.getFechaEmision())
                .createdAt(r.getCreatedAt())
                .cultivoId(r.getCultivo().getId())
                .cultivoNombre(r.getCultivo().getNombre())
                .tecnicoId(tecnico != null ? tecnico.getId() : null)
                .tecnicoNombre(tecnico != null ? "Técnico ID: " + tecnico.getUsuarioId() : null)
                .build();
    }
}
