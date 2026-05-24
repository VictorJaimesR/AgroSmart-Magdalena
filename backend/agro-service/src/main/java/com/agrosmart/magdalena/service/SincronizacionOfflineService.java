package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.domain.entity.SincronizacionOffline;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import com.agrosmart.magdalena.dto.request.ActividadRequest;
import com.agrosmart.magdalena.dto.request.CultivoRequest;
import com.agrosmart.magdalena.dto.request.FincaRequest;
import com.agrosmart.magdalena.dto.request.ParcelaRequest;
import com.agrosmart.magdalena.dto.request.SincronizacionRequest;
import com.agrosmart.magdalena.dto.response.CultivoResponse;
import com.agrosmart.magdalena.dto.response.FincaResponse;
import com.agrosmart.magdalena.dto.response.ParcelaResponse;
import com.agrosmart.magdalena.dto.response.SincronizacionResponse;
import com.agrosmart.magdalena.exception.BadRequestException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.ProductorRepository;
import com.agrosmart.magdalena.repository.SincronizacionOfflineRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacionOfflineService {

    private static final Set<String> METADATA_FIELDS = Set.of(
            "id", "clientId", "localId", "fincaNombre", "parcelaNombre",
            "isPending", "lastError", "timestamp"
    );

    private final SincronizacionOfflineRepository syncRepository;
    private final ProductorRepository productorRepository;
    private final FincaService fincaService;
    private final ParcelaService parcelaService;
    private final CultivoService cultivoService;
    private final ActividadService actividadService;
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
    public SincronizacionResponse registrar(SincronizacionRequest request, Long usuarioId,
                                             String email, String rol) {
        String clientId = normalize(request.getClientId());
        if (clientId != null) {
            var existente = syncRepository.findByUsuarioIdAndClientId(usuarioId, clientId);
            if (existente.isPresent()) {
                SincronizacionOffline sync = existente.get();
                if (EstadoSincronizacion.ERROR.equals(sync.getEstado())) {
                    sync.setEntidad(request.getEntidad());
                    sync.setAccion(request.getAccion());
                    sync.setDatosJson(request.getDatosJson());
                    sync.setEstado(EstadoSincronizacion.PENDIENTE);
                    sync.setMensajeError(null);
                    sync.setFechaSincronizacion(null);
                    sync.setServerId(null);
                    sync = syncRepository.save(sync);
                }
                return toResponse(sync);
            }
        }

        SincronizacionOffline sync = SincronizacionOffline.builder()
                .usuarioId(usuarioId)
                .clientId(clientId)
                .entidad(request.getEntidad())
                .accion(request.getAccion())
                .datosJson(request.getDatosJson())
                .build();

        sync = syncRepository.save(sync);
        return toResponse(sync);
    }

    @Transactional
    public List<SincronizacionResponse> procesarPendientes(Long usuarioId, String email, String rol) {
        List<SincronizacionOffline> pendientes = syncRepository
                .findByUsuarioIdAndEstadoOrderByCreatedAtAscIdAsc(usuarioId, EstadoSincronizacion.PENDIENTE);

        Map<String, Long> localIds = new HashMap<>();

        for (SincronizacionOffline sync : pendientes) {
            try {
                sync.setEstado(EstadoSincronizacion.EN_PROCESO);
                syncRepository.save(sync);

                log.info("Procesando sync #{}: {} {}", sync.getId(), sync.getAccion(), sync.getEntidad());

                ObjectNode data = readData(sync);
                processOperation(sync, data, usuarioId, email, rol, localIds);

                sync.setEstado(EstadoSincronizacion.SINCRONIZADO);
                sync.setMensajeError(null);
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

    private void processOperation(SincronizacionOffline sync, ObjectNode data,
                                   Long usuarioId, String email, String rol,
                                   Map<String, Long> localIds) throws Exception {
        String entidad = sync.getEntidad().toUpperCase();
        String accion  = sync.getAccion().toUpperCase();

        switch (entidad) {
            case "FINCA"     -> processFinca(sync, data, usuarioId, localIds, accion);
            case "PARCELA"   -> processParcela(sync, data, localIds, accion);
            case "CULTIVO"   -> processCultivo(sync, data, localIds, accion);
            case "ACTIVIDAD" -> processActividad(data, usuarioId, email, rol, localIds, accion);
            default -> throw new BadRequestException("Entidad offline no soportada: " + sync.getEntidad());
        }
    }

    private void processFinca(SincronizacionOffline sync, ObjectNode data,
                               Long usuarioId, Map<String, Long> localIds, String accion) throws Exception {
        switch (accion) {
            case "CREATE" -> {
                Long productorId = productorRepository.findByUsuarioId(usuarioId)
                        .map(Productor::getId)
                        .orElseThrow(() -> new ResourceNotFoundException("Productor", "usuarioId", usuarioId));
                FincaResponse response = fincaService.crear(productorId, toRequest(data, FincaRequest.class));
                rememberLocalId(localIds, sync, data, response.getId());
            }
            case "UPDATE" -> fincaService.actualizar(resolveId(data, "id", localIds), toRequest(data, FincaRequest.class));
            case "DELETE" -> fincaService.eliminar(resolveId(data, "id", localIds));
            default -> throw new BadRequestException("Accion de finca no soportada: " + accion);
        }
    }

    private void processParcela(SincronizacionOffline sync, ObjectNode data,
                                 Map<String, Long> localIds, String accion) throws Exception {
        resolveReference(data, "fincaId", localIds);
        switch (accion) {
            case "CREATE" -> {
                ParcelaResponse response = parcelaService.crear(toRequest(data, ParcelaRequest.class));
                rememberLocalId(localIds, sync, data, response.getId());
            }
            case "UPDATE" -> parcelaService.actualizar(resolveId(data, "id", localIds), toRequest(data, ParcelaRequest.class));
            case "DELETE" -> parcelaService.eliminar(resolveId(data, "id", localIds));
            default -> throw new BadRequestException("Accion de parcela no soportada: " + accion);
        }
    }

    private void processCultivo(SincronizacionOffline sync, ObjectNode data,
                                 Map<String, Long> localIds, String accion) throws Exception {
        resolveReference(data, "parcelaId", localIds);
        switch (accion) {
            case "CREATE" -> {
                CultivoResponse response = cultivoService.crear(toRequest(data, CultivoRequest.class));
                rememberLocalId(localIds, sync, data, response.getId());
            }
            case "UPDATE" -> cultivoService.actualizar(resolveId(data, "id", localIds), toRequest(data, CultivoRequest.class));
            case "DELETE" -> cultivoService.eliminar(resolveId(data, "id", localIds));
            default -> throw new BadRequestException("Accion de cultivo no soportada: " + accion);
        }
    }

    private void processActividad(ObjectNode data, Long usuarioId, String email, String rol,
                                   Map<String, Long> localIds, String accion) throws Exception {
        if (!"CREATE".equals(accion)) {
            throw new BadRequestException("Las actividades offline solo soportan CREATE");
        }
        resolveReference(data, "fincaId", localIds);
        resolveReference(data, "cultivoId", localIds);
        actividadService.registrar(toRequest(data, ActividadRequest.class), usuarioId, email, rol);
    }

    private ObjectNode readData(SincronizacionOffline sync) throws Exception {
        JsonNode node = objectMapper.readTree(sync.getDatosJson());
        if (!node.isObject()) {
            throw new BadRequestException("Los datos de sincronizacion deben ser un objeto JSON");
        }
        return (ObjectNode) node;
    }

    private <T> T toRequest(ObjectNode data, Class<T> requestType) throws Exception {
        ObjectNode clean = data.deepCopy();
        METADATA_FIELDS.forEach(clean::remove);
        return objectMapper.treeToValue(clean, requestType);
    }

    private void resolveReference(ObjectNode data, String field, Map<String, Long> localIds) {
        JsonNode value = data.get(field);
        if (value == null || value.isNull() || value.isNumber()) return;
        Long resolved = resolveId(data, field, localIds);
        data.put(field, resolved);
    }

    private Long resolveId(ObjectNode data, String field, Map<String, Long> localIds) {
        JsonNode value = data.get(field);
        if (value == null || value.isNull()) {
            throw new BadRequestException("Falta el identificador requerido: " + field);
        }
        if (value.isNumber()) return value.asLong();
        String raw = value.asText();
        if (localIds.containsKey(raw)) return localIds.get(raw);
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("No se pudo resolver el identificador local " + raw + " para " + field);
        }
    }

    private void rememberLocalId(Map<String, Long> localIds, SincronizacionOffline sync,
                                  ObjectNode data, Long serverId) {
        if (serverId == null) return;
        sync.setServerId(serverId);
        if (sync.getClientId() != null) localIds.put(sync.getClientId(), serverId);
        String localId = text(data, "localId");
        if (localId != null) localIds.put(localId, serverId);
    }

    private SincronizacionResponse toResponse(SincronizacionOffline s) {
        return SincronizacionResponse.builder()
                .id(s.getId())
                .clientId(s.getClientId())
                .localId(localId(s))
                .serverId(s.getServerId())
                .entidad(s.getEntidad())
                .accion(s.getAccion())
                .estado(s.getEstado().name())
                .mensajeError(s.getMensajeError())
                .createdAt(s.getCreatedAt())
                .fechaSincronizacion(s.getFechaSincronizacion())
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String text(ObjectNode data, String field) {
        JsonNode value = data.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String localId(SincronizacionOffline sync) {
        try {
            JsonNode node = objectMapper.readTree(sync.getDatosJson());
            JsonNode value = node.get("localId");
            return value == null || value.isNull() ? null : value.asText();
        } catch (Exception ignored) {
            return null;
        }
    }
}