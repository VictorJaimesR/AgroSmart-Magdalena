package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.SincronizacionOffline;
import com.agrosmart.magdalena.domain.entity.Usuario;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import com.agrosmart.magdalena.dto.request.SincronizacionRequest;
import com.agrosmart.magdalena.repository.SincronizacionOfflineRepository;
import com.agrosmart.magdalena.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SincronizacionOfflineService — Tests unitarios")
class SincronizacionOfflineServiceTest {

    @Mock
    private SincronizacionOfflineRepository syncRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FincaService fincaService;
    @Mock
    private ParcelaService parcelaService;
    @Mock
    private CultivoService cultivoService;

    @InjectMocks
    private SincronizacionOfflineService syncService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().nombreCompleto("Test User").email("test@sync.com").build();
        usuario.setId(1L);
    }

    @Test
    @DisplayName("Registrar sincronización individual")
    void registrar_exitoso() {
        SincronizacionRequest request = new SincronizacionRequest();
        request.setEntidad("FINCA");
        request.setAccion("CREATE");
        request.setDatosJson("{\"nombre\":\"Finca 1\"}");

        when(usuarioRepository.findByEmail("test@sync.com")).thenReturn(Optional.of(usuario));
        when(syncRepository.save(any(SincronizacionOffline.class))).thenAnswer(i -> {
            SincronizacionOffline saved = i.getArgument(0);
            saved.setId(1L);
            saved.setEstado(EstadoSincronizacion.PENDIENTE);
            return saved;
        });

        var response = syncService.registrar(request, "test@sync.com");

        assertThat(response).isNotNull();
        assertThat(response.getEntidad()).isEqualTo("FINCA");
        verify(syncRepository, times(1)).save(any(SincronizacionOffline.class));
    }

    @Test
    @DisplayName("Procesar pendientes llama al servicio correcto")
    void procesarPendientes_exitoso() throws Exception {
        SincronizacionOffline sync = SincronizacionOffline.builder()
                .usuario(usuario)
                .entidad("FINCA")
                .accion("CREATE")
                .datosJson("{\"nombre\":\"Finca 2\"}")
                .estado(EstadoSincronizacion.PENDIENTE)
                .build();
                
        when(usuarioRepository.findByEmail("test@sync.com")).thenReturn(Optional.of(usuario));
        when(syncRepository.findByUsuarioIdAndEstado(1L, EstadoSincronizacion.PENDIENTE))
                .thenReturn(List.of(sync));
        
        when(objectMapper.readValue(anyString(), eq(com.agrosmart.magdalena.dto.request.FincaRequest.class)))
                .thenReturn(new com.agrosmart.magdalena.dto.request.FincaRequest());
        
        syncService.procesarPendientesPorEmail("test@sync.com");
        
        verify(syncRepository, atLeast(1)).save(sync);
        assertThat(sync.getEstado()).isEqualTo(EstadoSincronizacion.SINCRONIZADO); // Error si objectmapper falla, SINCRONIZADO si pasa. Con el JSON inválido en el test, tirará error = true pero el flujo pasa.
    }
}
