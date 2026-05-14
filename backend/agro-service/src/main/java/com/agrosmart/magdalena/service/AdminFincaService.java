package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.*;
import com.agrosmart.magdalena.dto.response.AdminFincaResponse;
import com.agrosmart.magdalena.dto.response.AdminFincaDetalleResponse;
import com.agrosmart.magdalena.dto.response.AdminFincaDetalleResponse.ParcelaDetalleDto;
import com.agrosmart.magdalena.dto.response.AdminFincaDetalleResponse.CultivoDetalleDto;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio exclusivo para vistas ADMIN de fincas.
 * Proporciona información global y agregada sin capacidad de modificación.
 * NO modifica endpoints existentes para PRODUCTOR.
 */
@Service
@RequiredArgsConstructor
public class AdminFincaService {

    private final FincaRepository fincaRepository;
    private final ProductorRepository productorRepository;
    private final TecnicoAgropecuarioRepository tecnicoRepository;
    private final CultivoRepository cultivoRepository;
    private final ParcelaRepository parcelaRepository;

    /**
     * Listado global de fincas activas (solo lectura).
     */
    @Transactional(readOnly = true)
    public Page<AdminFincaResponse> listarFincasActivas(Pageable pageable) {
        return fincaRepository.findByActivoTrue(pageable)
                .map(this::toAdminResponse);
    }

    /**
     * Detalle completo de una finca incluyendo parcelas y cultivos.
     */
    @Transactional(readOnly = true)
    public AdminFincaDetalleResponse obtenerDetalleCompleto(Long fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca", "id", fincaId));

        if (!finca.getActivo()) {
            throw new ResourceNotFoundException("Finca", "id", fincaId);
        }

        return toAdminDetalleResponse(finca);
    }

    // ==================== MAPPERS ====================

    /**
     * Mapea Finca a AdminFincaResponse (para listado).
     */
    private AdminFincaResponse toAdminResponse(Finca finca) {
        // Obtener info del productor
        Productor productor = finca.getProductor();
        String productorNombre = productor != null
                ? (productor.getNombreCompleto() != null ? productor.getNombreCompleto() : "Productor #" + productor.getId())
                : "Productor desconocido";
        String productorCedula = productor != null ? productor.getCedula() : "-";

        // Obtener técnico asociado por municipio
        TecnicoInfo tecnicoInfo = obtenerTecnicoPorMunicipio(finca.getUbicacion().getMunicipio());

        // Contar parcelas
        long cantidadParcelas = parcelaRepository.countByFincaIdAndActivoTrue(finca.getId());

        // Contar cultivos y área cultivada
        List<Cultivo> cultivos = cultivoRepository.findByFincaId(finca.getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        long cantidadCultivos = cultivos.size();
        Double areaCultivada = cultivos.stream()
                .mapToDouble(c -> c.getAreaUtilizada() != null ? c.getAreaUtilizada() : 0)
                .sum();

        return AdminFincaResponse.builder()
                .id(finca.getId())
                .nombre(finca.getNombre())
                .areaTotal(finca.getAreaTotal())
                .unidadArea(finca.getUnidadArea())
                .municipio(finca.getUbicacion().getMunicipio())
                .departamento(finca.getUbicacion().getDepartamento())
                .vereda(finca.getUbicacion().getVereda())
                .productorId(productor != null ? productor.getId() : null)
                .productorNombre(productorNombre)
                .productorCedula(productorCedula)
                .tecnicoId(tecnicoInfo.id)
                .tecnicoNombre(tecnicoInfo.nombre)
                .estado("ACTIVO")
                .cantidadParcelas(cantidadParcelas)
                .cantidadCultivos(cantidadCultivos)
                .areaCultivada(areaCultivada)
                .createdAt(finca.getCreatedAt())
                .build();
    }

    /**
     * Mapea Finca a AdminFincaDetalleResponse (para detalle con parcelas y cultivos).
     */
    private AdminFincaDetalleResponse toAdminDetalleResponse(Finca finca) {
        // Obtener info del productor
        Productor productor = finca.getProductor();
        String productorNombre = productor != null
                ? (productor.getNombreCompleto() != null ? productor.getNombreCompleto() : "Productor #" + productor.getId())
                : "Productor desconocido";

        // Obtener técnico asociado por municipio
        TecnicoInfo tecnicoInfo = obtenerTecnicoPorMunicipio(finca.getUbicacion().getMunicipio());

        // Mapear parcelas con cultivos
        List<ParcelaDetalleDto> parcelasDto = finca.getParcelas().stream()
                .filter(Parcela::getActivo)
                .map(this::toParcelaDetalleDto)
                .collect(Collectors.toList());

        // Contar cultivos total
        long cantidadCultivos = finca.getParcelas().stream()
                .filter(Parcela::getActivo)
                .flatMap(p -> p.getCultivos().stream().filter(Cultivo::getActivo))
                .count();

        // Área cultivada
        Double areaCultivada = finca.getParcelas().stream()
                .filter(Parcela::getActivo)
                .flatMap(p -> p.getCultivos().stream().filter(Cultivo::getActivo))
                .mapToDouble(c -> c.getAreaUtilizada() != null ? c.getAreaUtilizada() : 0)
                .sum();

        return AdminFincaDetalleResponse.builder()
                .id(finca.getId())
                .nombre(finca.getNombre())
                .descripcion(finca.getDescripcion())
                .areaTotal(finca.getAreaTotal())
                .unidadArea(finca.getUnidadArea())
                .latitud(finca.getUbicacion().getLatitud())
                .longitud(finca.getUbicacion().getLongitud())
                .vereda(finca.getUbicacion().getVereda())
                .municipio(finca.getUbicacion().getMunicipio())
                .departamento(finca.getUbicacion().getDepartamento())
                .productorId(productor != null ? productor.getId() : null)
                .productorNombre(productorNombre)
                .productorCedula(productor != null ? productor.getCedula() : "-")
                .productorEmail(productor != null ? productor.getEmail() : "-")
                .productorTelefono(productor != null ? productor.getTelefono() : "-")
                .tecnicoId(tecnicoInfo.id)
                .tecnicoNombre(tecnicoInfo.nombre)
                .tecnicoEspecialidad(tecnicoInfo.especialidad)
                .estado("ACTIVO")
                .cantidadParcelas((long) finca.getParcelas().stream().filter(Parcela::getActivo).count())
                .cantidadCultivos(cantidadCultivos)
                .areaCultivada(areaCultivada)
                .parcelas(parcelasDto)
                .createdAt(finca.getCreatedAt())
                .build();
    }

    /**
     * Mapea Parcela a ParcelaDetalleDto con sus cultivos.
     */
    private ParcelaDetalleDto toParcelaDetalleDto(Parcela parcela) {
        List<CultivoDetalleDto> cultivosDto = parcela.getCultivos().stream()
                .filter(Cultivo::getActivo)
                .map(this::toCultivoDetalleDto)
                .collect(Collectors.toList());

        return ParcelaDetalleDto.builder()
                .id(parcela.getId())
                .nombre(parcela.getNombre())
                .areaParcela(parcela.getAreaParcela())
                .unidadArea(parcela.getUnidadArea())
                .tipoSuelo(parcela.getTipoSuelo())
                .descripcion(parcela.getDescripcion())
                .estado(parcela.getEstado() != null ? parcela.getEstado().toString() : "DESCONOCIDO")
                .cultivos(cultivosDto)
                .build();
    }

    /**
     * Mapea Cultivo a CultivoDetalleDto.
     */
    private CultivoDetalleDto toCultivoDetalleDto(Cultivo cultivo) {
        return CultivoDetalleDto.builder()
                .id(cultivo.getId())
                .nombre(cultivo.getNombre())
                .variedad(cultivo.getVariedad())
                .fechaSiembra(cultivo.getFechaSiembra())
                .fechaCosechaEstimada(cultivo.getFechaCosechaEstimada())
                .fechaCosechaReal(cultivo.getFechaCosechaReal())
                .areaUtilizada(cultivo.getAreaUtilizada())
                .estado(cultivo.getEstado() != null ? cultivo.getEstado().toString() : "DESCONOCIDO")
                .rendimientoEsperado(cultivo.getRendimientoEsperado())
                .rendimientoReal(cultivo.getRendimientoReal())
                .unidadRendimiento(cultivo.getUnidadRendimiento())
                .observaciones(cultivo.getObservaciones())
                .build();
    }

    // ==================== HELPERS ====================

    /**
     * Obtiene el técnico asociado a un municipio.
     * Busca un técnico cuyas zonas asignadas incluyan el municipio.
     */
    private TecnicoInfo obtenerTecnicoPorMunicipio(String municipio) {
        if (municipio == null || municipio.isEmpty()) {
            return TecnicoInfo.builder()
                    .id(null)
                    .nombre("Sin asignar")
                    .especialidad("-")
                    .build();
        }

        // Obtener todos los técnicos activos
        List<TecnicoAgropecuario> tecnicos = tecnicoRepository.findAll();

        // Buscar técnico cuyas zonas incluyan el municipio
        TecnicoAgropecuario tecnico = tecnicos.stream()
                .filter(t -> t.getActivo() && t.getZonasAsignadas() != null
                        && t.getZonasAsignadas().contains(municipio))
                .findFirst()
                .orElse(null);

        if (tecnico != null) {
            return TecnicoInfo.builder()
                    .id(tecnico.getId())
                    .nombre(obtenerNombreTecnico(tecnico))
                    .especialidad(tecnico.getEspecialidad())
                    .build();
        }

        return TecnicoInfo.builder()
                .id(null)
                .nombre("Sin asignar")
                .especialidad("-")
                .build();
    }

    /**
     * Obtiene el nombre del técnico desde la BD de auth-service.
     * Por ahora retorna nombre genérico con ID.
     */
    private String obtenerNombreTecnico(TecnicoAgropecuario tecnico) {
        // En una arquitectura microservicios real, consultarías auth-service
        // Por ahora, retornamos un identificador
        return "Técnico #" + tecnico.getId();
    }

    // ==================== INNER CLASSES ====================

    private static class TecnicoInfo {
        private Long id;
        private String nombre;
        private String especialidad;

        private TecnicoInfo(Long id, String nombre, String especialidad) {
            this.id = id;
            this.nombre = nombre;
            this.especialidad = especialidad;
        }

        public static TecnicoInfoBuilder builder() {
            return new TecnicoInfoBuilder();
        }

        public static class TecnicoInfoBuilder {
            private Long id;
            private String nombre;
            private String especialidad;

            public TecnicoInfoBuilder id(Long id) {
                this.id = id;
                return this;
            }

            public TecnicoInfoBuilder nombre(String nombre) {
                this.nombre = nombre;
                return this;
            }

            public TecnicoInfoBuilder especialidad(String especialidad) {
                this.especialidad = especialidad;
                return this;
            }

            public TecnicoInfo build() {
                return new TecnicoInfo(id, nombre, especialidad);
            }
        }
    }
}
