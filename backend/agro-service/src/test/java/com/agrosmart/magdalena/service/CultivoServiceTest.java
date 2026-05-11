package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Cultivo;
import com.agrosmart.magdalena.domain.entity.Finca;
import com.agrosmart.magdalena.domain.entity.Parcela;
import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.entity.UbicacionGeografica;
import com.agrosmart.magdalena.domain.entity.Usuario;
import com.agrosmart.magdalena.domain.enums.EstadoCultivo;
import com.agrosmart.magdalena.dto.request.CultivoRequest;
import com.agrosmart.magdalena.dto.response.CultivoResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.ParcelaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CultivoService — Tests unitarios")
class CultivoServiceTest {

    @Mock
    private CultivoRepository cultivoRepository;
    @Mock
    private ParcelaRepository parcelaRepository;

    @InjectMocks
    private CultivoService cultivoService;

    private Parcela parcela;
    private Cultivo cultivo;

    @BeforeEach
    void setUp() {
        Productor productor = Productor.builder().usuarioId(1L).cedula("123").build();
        productor.setId(1L);

        UbicacionGeografica ub = UbicacionGeografica.builder()
                .latitud(10.0).longitud(-74.0).municipio("Santa Marta").build();

        Finca finca = Finca.builder().nombre("Finca Test").productor(productor)
                .ubicacion(ub).areaTotal(10.0).build();
        finca.setId(1L);

        parcela = Parcela.builder().nombre("Lote Test").finca(finca).areaParcela(5.0).build();
        parcela.setId(1L);

        cultivo = Cultivo.builder()
                .nombre("Banano").variedad("Gran Enano").parcela(parcela)
                .fechaSiembra(LocalDate.of(2025, 1, 1))
                .fechaCosechaEstimada(LocalDate.of(2025, 10, 1))
                .estado(EstadoCultivo.SEMBRADO)
                .areaUtilizada(3.0).build();
        cultivo.setId(1L);
        cultivo.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Crear cultivo exitosamente")
    void crearCultivo_exitoso() {
        CultivoRequest request = new CultivoRequest();
        request.setNombre("Cacao");
        request.setVariedad("CCN-51");
        request.setParcelaId(1L);
        request.setFechaSiembra(LocalDate.of(2025, 3, 1));
        request.setAreaUtilizada(2.0);

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(cultivoRepository.sumAreaUtilizadaByParcelaId(1L)).thenReturn(3.0);
        when(cultivoRepository.save(any(Cultivo.class))).thenAnswer(inv -> {
            Cultivo c = inv.getArgument(0);
            c.setId(2L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        CultivoResponse response = cultivoService.crear(request);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Cacao");
        verify(cultivoRepository).save(any(Cultivo.class));
    }

    @Test
    @DisplayName("Crear cultivo falla si parcela no existe")
    void crearCultivo_parcelaNoExiste() {
        CultivoRequest request = new CultivoRequest();
        request.setNombre("Test");
        request.setParcelaId(999L);
        request.setFechaSiembra(LocalDate.now());

        when(parcelaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cultivoService.crear(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Crear cultivo falla si área excede parcela")
    void crearCultivo_areaExcede() {
        CultivoRequest request = new CultivoRequest();
        request.setNombre("Test");
        request.setParcelaId(1L);
        request.setFechaSiembra(LocalDate.now());
        request.setAreaUtilizada(10.0); // Excede 5.0 de parcela

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));
        when(cultivoRepository.sumAreaUtilizadaByParcelaId(1L)).thenReturn(0.0);

        assertThatThrownBy(() -> cultivoService.crear(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("excede");
    }

    @Test
    @DisplayName("Crear cultivo falla si fecha siembra posterior a cosecha")
    void crearCultivo_fechasInvalidas() {
        CultivoRequest request = new CultivoRequest();
        request.setNombre("Test");
        request.setParcelaId(1L);
        request.setFechaSiembra(LocalDate.of(2026, 1, 1));
        request.setFechaCosechaEstimada(LocalDate.of(2025, 1, 1));

        when(parcelaRepository.findById(1L)).thenReturn(Optional.of(parcela));

        assertThatThrownBy(() -> cultivoService.crear(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("fecha de siembra");
    }

    @Test
    @DisplayName("Obtener cultivo por ID exitoso")
    void obtenerPorId_exitoso() {
        when(cultivoRepository.findById(1L)).thenReturn(Optional.of(cultivo));

        CultivoResponse response = cultivoService.obtenerPorId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Banano");
    }

    @Test
    @DisplayName("Eliminar cultivo (soft delete)")
    void eliminarCultivo() {
        when(cultivoRepository.findById(1L)).thenReturn(Optional.of(cultivo));
        when(cultivoRepository.save(any())).thenReturn(cultivo);

        cultivoService.eliminar(1L);

        assertThat(cultivo.getActivo()).isFalse();
        verify(cultivoRepository).save(cultivo);
    }
}
