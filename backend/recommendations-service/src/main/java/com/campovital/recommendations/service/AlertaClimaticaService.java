package com.campovital.recommendations.service;

import com.campovital.recommendations.domain.entity.AlertaClimatica;
import com.campovital.recommendations.domain.entity.Usuario;
import com.campovital.recommendations.domain.enums.TipoAlerta;
import com.campovital.recommendations.dto.request.AlertaClimaticaRequest;
import com.campovital.recommendations.dto.response.AlertaClimaticaResponse;
import com.campovital.recommendations.exception.BadRequestException;
import com.campovital.recommendations.exception.ResourceNotFoundException;
import com.campovital.recommendations.repository.AlertaClimaticaRepository;
import com.campovital.recommendations.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaClimaticaService {

    private final AlertaClimaticaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<AlertaClimaticaResponse> listarActivas(Pageable pageable) {
        return alertaRepository.findByActivaTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AlertaClimaticaResponse> listarHistorial(Pageable pageable) {
        return alertaRepository.findAllByOrderByFechaEmisionDesc(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AlertaClimaticaResponse> listarPorMunicipio(String municipio) {
        return alertaRepository.findActivasByMunicipio(municipio).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertaClimaticaResponse obtenerPorId(Long id) {
        AlertaClimatica alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertaClimatica", "id", id));
        return toResponse(alerta);
    }

    @Transactional
    public AlertaClimaticaResponse crear(AlertaClimaticaRequest request, String emailUsuario) {
        Usuario emisor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        TipoAlerta tipo;
        try {
            tipo = TipoAlerta.valueOf(request.getTipo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de alerta inválido: " + request.getTipo());
        }

        AlertaClimatica alerta = AlertaClimatica.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipo(tipo)
                .fechaExpiracion(request.getFechaExpiracion())
                .municipiosAfectados(request.getMunicipiosAfectados() != null ? request.getMunicipiosAfectados() : List.of())
                .emitidaPor(emisor)
                .build();

        alerta = alertaRepository.save(alerta);
        return toResponse(alerta);
    }

    @Transactional
    public void desactivar(Long id) {
        AlertaClimatica alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertaClimatica", "id", id));
        alerta.setActiva(false);
        alertaRepository.save(alerta);
    }

    private AlertaClimaticaResponse toResponse(AlertaClimatica a) {
        return AlertaClimaticaResponse.builder()
                .id(a.getId())
                .titulo(a.getTitulo())
                .descripcion(a.getDescripcion())
                .tipo(a.getTipo().name())
                .fechaEmision(a.getFechaEmision())
                .fechaExpiracion(a.getFechaExpiracion())
                .municipiosAfectados(a.getMunicipiosAfectados())
                .activa(a.getActiva())
                .estadoAlerta(a.getEstadoAlerta() != null ? a.getEstadoAlerta().name() : null)
                .generadaPorId(a.getEmitidaPor() != null ? a.getEmitidaPor().getId() : null)
                .createdAt(a.getCreatedAt())
                .emitidaPor(a.getEmitidaPor() != null ? a.getEmitidaPor().getNombreCompleto() : null)
                .build();
    }
}
