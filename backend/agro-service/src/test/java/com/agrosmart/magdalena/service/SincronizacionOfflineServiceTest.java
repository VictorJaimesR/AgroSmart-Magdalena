package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.SincronizacionOffline;
import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import com.agrosmart.magdalena.dto.request.SincronizacionRequest;
import com.agrosmart.magdalena.dto.response.FincaResponse;
import com.agrosmart.magdalena.dto.response.SincronizacionResponse;
import com.agrosmart.magdalena.repository.ProductorRepository;
import com.agrosmart.magdalena.repository.SincronizacionOfflineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SincronizacionOfflineService — Tests unitarios")
class SincronizacionOfflineServiceTest {

    @Mock private SincronizacionOfflineRepository syncRepository;
    @Mock private ProductorRepository productorRepository;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();
    @Mock private FincaService fincaService;
    @Mock private ParcelaService parcelaService;
    @Mock private CultivoService cultivoService;
    @Mock private ActividadService actividadService;

    @InjectMocks
    private SincronizacionOfflineService syncService;

    private static final Long USUARIO_ID = 1L;
    private static final String EMAIL     = "test@sync.com";
    private static final String ROL       = "AGRICULTOR";

    @Test
    @DisplayName("Registrar sincronización individual")
    void registrar_exitoso() {
        SincronizacionRequest request = new SincronizacionRequest();
        request.setEntidad("FINCA");
        request.setAccion("CREATE");
        request.setDatosJson("{\"nombre\":\"Finca 1\"}");

        when(syncRepository.save(any(SincronizacionOffline.class))).thenAnswer(i -> {
            SincronizacionOffline saved = i.getArgument(0);
            saved.setId(1L);
            saved.setEstado(EstadoSincronizacion.PENDIENTE);
            return saved;
        });

        SincronizacionResponse response = syncService.registrar(request, USUARIO_ID, EMAIL, ROL);

        assertThat(response).isNotNull();
        assertThat(response.getEntidad()).isEqualTo("FINCA");
        verify(syncRepository, times(1)).save(any(SincronizacionOffline.class));
    }

    @Test
    @DisplayName("Procesar pendientes llama al servicio correcto")
    void procesarPendientes_exitoso() throws Exception {
        SincronizacionOffline sync = SincronizacionOffline.builder()
                .usuarioId(USUARIO_ID)
                .entidad("FINCA")
                .accion("CREATE")
                .datosJson("{\"nombre\":\"Finca 2\"}")
                .estado(EstadoSincronizacion.PENDIENTE)
                .build();

        when(syncRepository.findByUsuarioIdAndEstadoOrderByCreatedAtAscIdAsc(
                USUARIO_ID, EstadoSincronizacion.PENDIENTE))
                .thenReturn(List.of(sync));

        Productor productor = new Productor();
        productor.setId(10L);
        when(productorRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(productor));
        when(fincaService.crear(eq(10L), any())).thenReturn(FincaResponse.builder().id(20L).build());
        when(syncRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        syncService.procesarPendientes(USUARIO_ID, EMAIL, ROL);

        verify(syncRepository, atLeast(1)).save(sync);
        assertThat(sync.getEstado()).isEqualTo(EstadoSincronizacion.SINCRONIZADO);
    }
}