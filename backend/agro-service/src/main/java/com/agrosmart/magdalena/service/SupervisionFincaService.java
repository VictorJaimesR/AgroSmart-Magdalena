package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Finca;
import com.agrosmart.magdalena.domain.entity.SupervisionFinca;
import com.agrosmart.magdalena.domain.enums.EstadoSupervision;
import com.agrosmart.magdalena.dto.response.FincaDisponibleTecnicoResponse;
import com.agrosmart.magdalena.dto.response.FincaSupervisadaResponse;
import com.agrosmart.magdalena.dto.response.SupervisionFincaResponse;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.SupervisionFincaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupervisionFincaService {

    private final SupervisionFincaRepository supervisionRepo;
    private final FincaRepository fincaRepository;

        @Transactional(readOnly = true)
        public List<FincaDisponibleTecnicoResponse> listarFincasDisponibles() {
        List<Finca> fincas = fincaRepository.findAll().stream().filter(Finca::getActivo).collect(Collectors.toList());
        return fincas.stream()
            .filter(f -> !supervisionRepo.existsByFincaIdAndEstado(f.getId(), EstadoSupervision.ACTIVA))
            .map(f -> FincaDisponibleTecnicoResponse.builder()
                .id(f.getId())
                .nombre(f.getNombre())
                .areaTotal(f.getAreaTotal())
                .municipio(f.getUbicacion() != null ? f.getUbicacion().getMunicipio() : null)
                .vereda(f.getUbicacion() != null ? f.getUbicacion().getVereda() : null)
                .productorUsuarioId(f.getProductor() != null ? f.getProductor().getUsuarioId() : null)
                .productorNombre(f.getProductor() != null ? f.getProductor().getNombreCompleto() : null)
                .build())
            .collect(Collectors.toList());
        }

    @Transactional
    public SupervisionFincaResponse tomarFinca(Long fincaId, Long tecnicoUsuarioId, String tecnicoNombre, String tecnicoEmail) {
        if (supervisionRepo.existsByFincaIdAndEstado(fincaId, EstadoSupervision.ACTIVA)) {
            throw new IllegalStateException("Ya existe una supervisión activa para esta finca");
        }

        Finca finca = fincaRepository.findById(fincaId).orElseThrow(() -> new IllegalArgumentException("Finca no encontrada"));

        SupervisionFinca s = SupervisionFinca.builder()
                .finca(finca)
                .tecnicoUsuarioId(tecnicoUsuarioId)
                .tecnicoNombre(tecnicoNombre)
                .tecnicoEmail(tecnicoEmail)
                .fechaInicio(LocalDateTime.now())
                .estado(EstadoSupervision.ACTIVA)
                .activo(true)
                .build();

        SupervisionFinca saved = supervisionRepo.save(s);

        return SupervisionFincaResponse.builder()
                .id(saved.getId())
                .fincaId(finca.getId())
                .fincaNombre(finca.getNombre())
                .tecnicoUsuarioId(saved.getTecnicoUsuarioId())
                .tecnicoNombre(saved.getTecnicoNombre())
                .tecnicoEmail(saved.getTecnicoEmail())
                .fechaInicio(saved.getFechaInicio())
                .estado(saved.getEstado().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FincaSupervisadaResponse> listarMisFincas(Long tecnicoUsuarioId) {
        List<SupervisionFinca> list = supervisionRepo.findByTecnicoUsuarioIdAndEstado(tecnicoUsuarioId, EstadoSupervision.ACTIVA);
        return list.stream().map(s -> FincaSupervisadaResponse.builder()
                .supervisionId(s.getId())
                .fincaId(s.getFinca().getId())
                .fincaNombre(s.getFinca().getNombre())
                .productorUsuarioId(s.getFinca().getProductor() != null ? s.getFinca().getProductor().getUsuarioId() : null)
                .productorNombre(s.getFinca().getProductor() != null ? s.getFinca().getProductor().getNombreCompleto() : null)
                .municipio(s.getFinca().getUbicacion() != null ? s.getFinca().getUbicacion().getMunicipio() : null)
                .areaTotal(s.getFinca().getAreaTotal())
                .fechaInicio(s.getFechaInicio())
                .estado(s.getEstado() != null ? s.getEstado().name() : null)
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public SupervisionFincaResponse finalizar(Long supervisionId) {
        SupervisionFinca s = supervisionRepo.findById(supervisionId).orElseThrow(() -> new IllegalArgumentException("Supervisión no encontrada"));
        s.setEstado(EstadoSupervision.FINALIZADA);
        s.setFechaFin(LocalDateTime.now());
        s.setActivo(false);
        SupervisionFinca saved = supervisionRepo.save(s);

        return SupervisionFincaResponse.builder()
                .id(saved.getId())
                .fincaId(saved.getFinca().getId())
                .fincaNombre(saved.getFinca().getNombre())
                .tecnicoUsuarioId(saved.getTecnicoUsuarioId())
                .tecnicoNombre(saved.getTecnicoNombre())
                .tecnicoEmail(saved.getTecnicoEmail())
                .fechaInicio(saved.getFechaInicio())
                .fechaFin(saved.getFechaFin())
                .estado(saved.getEstado().name())
                .build();
    }
}
