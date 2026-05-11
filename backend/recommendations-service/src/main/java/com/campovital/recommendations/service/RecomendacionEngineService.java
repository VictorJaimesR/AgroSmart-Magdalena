package com.campovital.recommendations.service;

import com.campovital.recommendations.domain.entity.Cultivo;
import com.campovital.recommendations.domain.entity.Recomendacion;
import com.campovital.recommendations.exception.ResourceNotFoundException;
import com.campovital.recommendations.repository.CultivoRepository;
import com.campovital.recommendations.repository.RecomendacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecomendacionEngineService {

    private final RecomendacionRepository recomendacionRepository;
    private final CultivoRepository cultivoRepository;

    @Transactional
    public void generarRecomendacionesAutomaticas(Long cultivoId, String nuevoEstado) {
        Cultivo cultivo = cultivoRepository.findById(cultivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo", "id", cultivoId));

        log.info("Motor de recomendaciones detectó cambio de estado a {} para el cultivo ID {}", nuevoEstado, cultivo.getId());

        String titulo;
        String descripcion;

        switch (nuevoEstado) {
            case "SEMBRADO" -> {
                titulo = "Fertilización de Establecimiento y Riego";
                descripcion = "Se recomienda aplicar fertilizante rico en Fósforo y mantener el lote " + cultivo.getParcela().getNombre() + " con humedad de capacidad de campo durante la primera semana.";
            }
            case "EN_CRECIMIENTO" -> {
                titulo = "Control Preventivo de Plagas y Malezas";
                descripcion = "Dado el desarrollo foliar, es prioridad realizar desyerba química o manual y aplicar fungicida preventivo.";
            }
            case "EN_COSECHA" -> {
                titulo = "Protocolo de Recolección Segura";
                descripcion = "Inicie la recolección en las horas más frescas de la mañana. No prolongue el acopio en campo bajo el sol de forma directa.";
            }
            default -> {
                return;
            }
        }

        Recomendacion recomendacion = Recomendacion.builder()
                .titulo("Autogenerado: " + titulo)
                .descripcion(descripcion)
                .cultivo(cultivo)
                .aplicada(false)
                .build();

        recomendacionRepository.save(recomendacion);
        log.info("Recomendación autogenerada y guardada exitosamente.");
    }
}
