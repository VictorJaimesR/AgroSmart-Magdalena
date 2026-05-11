package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.*;
import com.agrosmart.magdalena.dto.request.FincaRequest;
import com.agrosmart.magdalena.dto.response.FincaResponse;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ParcelaRepository;
import com.agrosmart.magdalena.repository.ProductorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FincaService — Tests unitarios")
class FincaServiceTest {

    @Mock private FincaRepository fincaRepository;
    @Mock private ProductorRepository productorRepository;
    @Mock private ParcelaRepository parcelaRepository;

    @InjectMocks private FincaService fincaService;

    private Productor productor;
    private Finca finca;

    @BeforeEach
    void setUp() {
        productor = Productor.builder().usuarioId(1L).cedula("123").build();
        productor.setId(1L);

        UbicacionGeografica ub = UbicacionGeografica.builder()
                .latitud(10.4).longitud(-74.1).municipio("Santa Marta").departamento("Magdalena").build();
        ub.setId(1L);

        finca = Finca.builder().nombre("Finca Test").productor(productor)
                .ubicacion(ub).areaTotal(10.0).build();
        finca.setId(1L);
        finca.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Crear finca exitosamente")
    void crearFinca_exitoso() {
        FincaRequest request = new FincaRequest();
        request.setNombre("Finca Nueva");
        request.setAreaTotal(20.0);
        request.setLatitud(10.5);
        request.setLongitud(-74.2);
        request.setMunicipio("Ciénaga");

        when(productorRepository.findById(1L)).thenReturn(Optional.of(productor));
        when(fincaRepository.save(any(Finca.class))).thenAnswer(inv -> {
            Finca f = inv.getArgument(0);
            f.setId(2L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });
        when(parcelaRepository.countByFincaIdAndActivoTrue(any())).thenReturn(0L);

        FincaResponse response = fincaService.crear(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Finca Nueva");
        assertThat(response.getMunicipio()).isEqualTo("Ciénaga");
        verify(fincaRepository).save(any(Finca.class));
    }

    @Test
    @DisplayName("Crear finca falla si productor no existe")
    void crearFinca_productorNoExiste() {
        FincaRequest request = new FincaRequest();
        request.setNombre("Test");
        request.setAreaTotal(5.0);
        request.setLatitud(10.0);
        request.setLongitud(-74.0);
        request.setMunicipio("Test");

        when(productorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fincaService.crear(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Obtener finca por ID")
    void obtenerPorId_exitoso() {
        when(fincaRepository.findById(1L)).thenReturn(Optional.of(finca));
        when(parcelaRepository.countByFincaIdAndActivoTrue(1L)).thenReturn(2L);

        FincaResponse response = fincaService.obtenerPorId(1L);

        assertThat(response.getNombre()).isEqualTo("Finca Test");
        assertThat(response.getCantidadParcelas()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Soft delete de finca")
    void eliminarFinca() {
        when(fincaRepository.findById(1L)).thenReturn(Optional.of(finca));
        when(fincaRepository.save(any())).thenReturn(finca);

        fincaService.eliminar(1L);

        assertThat(finca.getActivo()).isFalse();
    }
}
