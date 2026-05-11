package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Finca;
import com.agrosmart.magdalena.domain.entity.Parcela;
import com.agrosmart.magdalena.domain.enums.EstadoParcela;
import com.agrosmart.magdalena.dto.request.ParcelaRequest;
import com.agrosmart.magdalena.dto.response.ParcelaResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ParcelaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de parcelas/lotes.
 *
 * Reglas de negocio:
 *   - Una parcela pertenece a exactamente una finca
 *   - La suma de áreas de parcelas no debe exceder el área total de la finca
 */
@Service
@RequiredArgsConstructor
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final FincaRepository fincaRepository;

    @Transactional(readOnly = true)
    public Page<ParcelaResponse> listarPorFinca(Long fincaId, Pageable pageable) {
        return parcelaRepository.findByFincaIdAndActivoTrue(fincaId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ParcelaResponse obtenerPorId(Long id) {
        Parcela parcela = buscarParcelaActiva(id);
        return toResponse(parcela);
    }

    @Transactional
    public ParcelaResponse crear(ParcelaRequest request) {
        Finca finca = fincaRepository.findById(request.getFincaId())
                .orElseThrow(() -> new ResourceNotFoundException("Finca", "id", request.getFincaId()));

        // Validar que el área de la parcela no exceda el área disponible de la finca
        validarAreaDisponible(finca, request.getAreaParcela(), null);

        Parcela parcela = Parcela.builder()
                .nombre(request.getNombre())
                .finca(finca)
                .areaParcela(request.getAreaParcela())
                .unidadArea(request.getUnidadArea() != null ? request.getUnidadArea() : "hectáreas")
                .tipoSuelo(request.getTipoSuelo())
                .descripcion(request.getDescripcion())
                .build();

        parcela = parcelaRepository.save(parcela);
        return toResponse(parcela);
    }

    @Transactional
    public ParcelaResponse actualizar(Long id, ParcelaRequest request) {
        Parcela parcela = buscarParcelaActiva(id);

        validarAreaDisponible(parcela.getFinca(), request.getAreaParcela(), id);

        parcela.setNombre(request.getNombre());
        parcela.setAreaParcela(request.getAreaParcela());
        if (request.getUnidadArea() != null) parcela.setUnidadArea(request.getUnidadArea());
        parcela.setTipoSuelo(request.getTipoSuelo());
        parcela.setDescripcion(request.getDescripcion());

        parcela = parcelaRepository.save(parcela);
        return toResponse(parcela);
    }

    @Transactional
    public void eliminar(Long id) {
        Parcela parcela = buscarParcelaActiva(id);
        parcela.setActivo(false);
        parcelaRepository.save(parcela);
    }

    /**
     * Valida que la nueva área no exceda el área total disponible de la finca.
     * Excluye la parcela actual (excludeId) si es una actualización.
     */
    private void validarAreaDisponible(Finca finca, Double nuevaArea, Long excludeId) {
        // Obtener suma total de áreas usando la query del repositorio
        Double areaOcupada = parcelaRepository.sumAreaByFincaId(finca.getId());
        if (areaOcupada == null) {
            areaOcupada = 0.0;
        }

        // Si es una actualización, restar el área anterior de esta parcela
        if (excludeId != null) {
            Parcela parcelaActual = parcelaRepository.findById(excludeId).orElse(null);
            if (parcelaActual != null) {
                areaOcupada -= parcelaActual.getAreaParcela();
            }
        }

        if (areaOcupada + nuevaArea > finca.getAreaTotal()) {
            throw new BadRequestException(String.format(
                    "El área de la parcela supera el área disponible de la finca. "
                            + "Área solicitada: %.2f, Área disponible: %.2f de %.2f %s",
                    nuevaArea, finca.getAreaTotal() - areaOcupada, finca.getAreaTotal(), finca.getUnidadArea()));
        }
    }

    private Parcela buscarParcelaActiva(Long id) {
        Parcela parcela = parcelaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", "id", id));
        if (!parcela.getActivo()) {
            throw new ResourceNotFoundException("Parcela", "id", id);
        }
        return parcela;
    }

    private ParcelaResponse toResponse(Parcela p) {
        long cantidadCultivos = p.getCultivos() != null
                ? p.getCultivos().stream().filter(c -> c.getActivo()).count()
                : 0;
        return ParcelaResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .areaParcela(p.getAreaParcela())
                .unidadArea(p.getUnidadArea())
                .tipoSuelo(p.getTipoSuelo())
                .descripcion(p.getDescripcion())
                .estado(p.getEstado() != null ? p.getEstado().name() : EstadoParcela.DISPONIBLE.name())
                .activo(p.getActivo())
                .createdAt(p.getCreatedAt())
                .fincaId(p.getFinca().getId())
                .fincaNombre(p.getFinca().getNombre())
                .cantidadCultivos(cantidadCultivos)
                .build();
    }
}
