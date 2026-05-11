package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.SincronizacionOffline;
import com.agrosmart.magdalena.domain.entity.Usuario;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import com.agrosmart.magdalena.dto.request.SincronizacionRequest;
import com.agrosmart.magdalena.dto.response.SincronizacionResponse;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.SincronizacionOfflineRepository;
import com.agrosmart.magdalena.repository.UsuarioRepository;
import com.agrosmart.magdalena.dto.request.FincaRequest;
import com.agrosmart.magdalena.dto.request.ParcelaRequest;
import com.agrosmart.magdalena.dto.request.CultivoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacionOfflineService {

    private final SincronizacionOfflineRepository syncRepository;
    private final UsuarioRepository usuarioRepository;
    private final FincaService fincaService;
    private final ParcelaService parcelaService;
    private final CultivoService cultivoService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<SincronizacionResponse> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return syncRepository.findByUsuarioId(usuarioId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SincronizacionResponse> listarPendientes(Long usuarioId) {
        return syncRepository.findByUsuarioIdAndEstado(usuarioId, EstadoSincronizacion.PENDIENTE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SincronizacionResponse registrar(SincronizacionRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailUsuario));

        SincronizacionOffline sync = SincronizacionOffline.builder()
                .usuario(usuario)
                .entidad(request.getEntidad())
                .accion(request.getAccion())
                .datosJson(request.getDatosJson())
                .build();

        sync = syncRepository.save(sync);
        return toResponse(sync);
    }

    @Transactional
    public List<SincronizacionResponse> procesarPendientesPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return procesarPendientes(usuario.getId());
    }

    @Transactional
    public List<SincronizacionResponse> procesarPendientes(Long usuarioId) {
        List<SincronizacionOffline> pendientes = syncRepository
                .findByUsuarioIdAndEstado(usuarioId, EstadoSincronizacion.PENDIENTE);

        for (SincronizacionOffline sync : pendientes) {
            try {
                sync.setEstado(EstadoSincronizacion.EN_PROCESO);
                syncRepository.save(sync);
                
                log.info("Procesando sync #{}: {} {}", sync.getId(), sync.getAccion(), sync.getEntidad());
                
                if (sync.getEntidad().equals("FINCA")) {
                    FincaRequest req = objectMapper.readValue(sync.getDatosJson(), FincaRequest.class);
                    if (sync.getAccion().equals("CREATE")) fincaService.crear(usuarioId, req);
                    // UPDATE would check target ID but offline strategy usually requires IDs assigned by server
                } else if (sync.getEntidad().equals("PARCELA")) {
                    ParcelaRequest req = objectMapper.readValue(sync.getDatosJson(), ParcelaRequest.class);
                    if (sync.getAccion().equals("CREATE")) parcelaService.crear(req);
                } else if (sync.getEntidad().equals("CULTIVO")) {
                    CultivoRequest req = objectMapper.readValue(sync.getDatosJson(), CultivoRequest.class);
                    if (sync.getAccion().equals("CREATE")) cultivoService.crear(req);
                }

                sync.setEstado(EstadoSincronizacion.SINCRONIZADO);
                sync.setFechaSincronizacion(LocalDateTime.now());
            } catch (Exception e) {
                sync.setEstado(EstadoSincronizacion.ERROR);
                sync.setMensajeError(e.getMessage() != null ? e.getMessage() : "Error desconocido");
                log.error("Error procesando sync #{}: {}", sync.getId(), e.getMessage());
            }
            syncRepository.save(sync);
        }

        return pendientes.stream().map(this::toResponse).toList();
    }

    private SincronizacionResponse toResponse(SincronizacionOffline s) {
        return SincronizacionResponse.builder()
                .id(s.getId()).entidad(s.getEntidad()).accion(s.getAccion())
                .estado(s.getEstado().name()).mensajeError(s.getMensajeError())
                .createdAt(s.getCreatedAt()).fechaSincronizacion(s.getFechaSincronizacion())
                .build();
    }
}
