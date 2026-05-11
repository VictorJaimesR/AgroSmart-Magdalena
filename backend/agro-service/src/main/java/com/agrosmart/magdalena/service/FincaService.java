package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Finca;
import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.entity.Parcela;
import com.agrosmart.magdalena.domain.entity.UbicacionGeografica;
import com.agrosmart.magdalena.dto.request.FincaRequest;
import com.agrosmart.magdalena.dto.response.FincaResponse;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ParcelaRepository;
import com.agrosmart.magdalena.repository.ProductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de fincas.
 *
 * Reglas de negocio:
 *   - Una finca pertenece a exactamente un productor
 *   - Cada finca tiene ubicación geográfica obligatoria
 *   - Soporta soft delete
 */
@Service
@RequiredArgsConstructor
public class FincaService {

    private final FincaRepository fincaRepository;
    private final ProductorRepository productorRepository;
    private final ParcelaRepository parcelaRepository;

    @Transactional(readOnly = true)
    public Page<FincaResponse> listarTodas(Pageable pageable) {
        return fincaRepository.findByActivoTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FincaResponse> listarPorProductor(Long productorId, Pageable pageable) {
        return fincaRepository.findByProductorIdAndActivoTrue(productorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FincaResponse obtenerPorId(Long id) {
        Finca finca = buscarFincaActiva(id);
        return toResponse(finca);
    }

    @Transactional(readOnly = true)
    public List<FincaResponse> buscarPorMunicipio(String municipio) {
        return fincaRepository.findByMunicipio(municipio).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FincaResponse crear(Long productorId, FincaRequest request) {
        Productor productor = productorRepository.findById(productorId)
            .orElseThrow(() -> new ResourceNotFoundException("Productor", "id", productorId));

        if (request.getAreaTotal() != null && request.getAreaTotal() <= 0) {
            throw new com.agrosmart.magdalena.exception.BadRequestException("El área total de la finca debe ser mayor a 0");
        }

        UbicacionGeografica ubicacion = UbicacionGeografica.builder()
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .vereda(request.getVereda())
                .municipio(request.getMunicipio())
                .referenciaAdicional(request.getReferenciaAdicional())
                .build();

        Finca finca = Finca.builder()
                .nombre(request.getNombre())
                .productor(productor)
                .ubicacion(ubicacion)
                .areaTotal(request.getAreaTotal())
                .unidadArea(request.getUnidadArea() != null ? request.getUnidadArea() : "hectáreas")
                .descripcion(request.getDescripcion())
                .build();

        finca = fincaRepository.save(finca);

        // Auto-crear parcela por defecto
        Parcela parcela = Parcela.builder()
                .nombre("Lote Principal")
                .finca(finca)
                .areaParcela(finca.getAreaTotal())
                .unidadArea(finca.getUnidadArea())
                .descripcion("Parcela principal autogenerada")
                .activo(true)
                .build();
        parcelaRepository.save(parcela);

        return toResponse(finca);
    }

    @Transactional
    public FincaResponse actualizar(Long id, FincaRequest request) {
        Finca finca = buscarFincaActiva(id);

        if (request.getAreaTotal() != null && request.getAreaTotal() <= 0) {
            throw new com.agrosmart.magdalena.exception.BadRequestException("El área total de la finca debe ser mayor a 0");
        }

        finca.setNombre(request.getNombre());
        finca.setAreaTotal(request.getAreaTotal());
        if (request.getUnidadArea() != null) finca.setUnidadArea(request.getUnidadArea());
        finca.setDescripcion(request.getDescripcion());

        // Actualizar ubicación
        UbicacionGeografica ubicacion = finca.getUbicacion();
        ubicacion.setLatitud(request.getLatitud());
        ubicacion.setLongitud(request.getLongitud());
        ubicacion.setVereda(request.getVereda());
        ubicacion.setMunicipio(request.getMunicipio());
        ubicacion.setReferenciaAdicional(request.getReferenciaAdicional());

        finca = fincaRepository.save(finca);
        return toResponse(finca);
    }

    /**
     * Soft delete: marca la finca como inactiva.
     */
    @Transactional
    public void eliminar(Long id) {
        Finca finca = buscarFincaActiva(id);
        finca.setActivo(false);
        fincaRepository.save(finca);
    }

    private Finca buscarFincaActiva(Long id) {
        Finca finca = fincaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", "id", id));
        if (!finca.getActivo()) {
            throw new ResourceNotFoundException("Finca", "id", id);
        }
        return finca;
    }

    private FincaResponse toResponse(Finca f) {
        long cantidadParcelas = parcelaRepository.countByFincaIdAndActivoTrue(f.getId());
        return FincaResponse.builder()
                .id(f.getId())
                .nombre(f.getNombre())
                .areaTotal(f.getAreaTotal())
                .unidadArea(f.getUnidadArea())
                .descripcion(f.getDescripcion())
                .activo(f.getActivo())
                .createdAt(f.getCreatedAt())
                .ubicacionId(f.getUbicacion().getId())
                .latitud(f.getUbicacion().getLatitud())
                .longitud(f.getUbicacion().getLongitud())
                .vereda(f.getUbicacion().getVereda())
                .municipio(f.getUbicacion().getMunicipio())
                .departamento(f.getUbicacion().getDepartamento())
                .productorId(f.getProductor() != null ? f.getProductor().getId() : null)
                .productorNombre(f.getProductor() != null 
                        ? (f.getProductor().getNombreCompleto() != null
                                ? f.getProductor().getNombreCompleto()
                                : "Productor #" + f.getProductor().getId())
                        : "Productor desconocido")
                .cantidadParcelas(cantidadParcelas)
                .build();
    }
}
