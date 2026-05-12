package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.CultivoRepository;
import com.agrosmart.magdalena.repository.FincaRepository;
import com.agrosmart.magdalena.repository.ProductorRepository;
import com.agrosmart.magdalena.repository.SincronizacionOfflineRepository;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final FincaRepository fincaRepository;
    private final CultivoRepository cultivoRepository;
    private final ProductorRepository productorRepository;
    private final SincronizacionOfflineRepository syncRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public Map<String, Object> getProductorStats(String email, Long usuarioId) {
        log.info("getProductorStats called with email={}, usuarioId={}", email, usuarioId);
        Productor productor = null;
        if (usuarioId != null) {
            log.info("Trying to find Productor by usuarioId={}", usuarioId);
            productor = productorRepository.findByUsuarioId(usuarioId)
                .orElse(null);
            if (productor != null) {
                log.info("Found Productor by usuarioId: productorId={}", productor.getId());
            }
        }
        if (productor == null) {
            log.info("Trying to find Productor by email={}", email);
            productor = productorRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("No Productor found for email={}", email);
                    return new ResourceNotFoundException(
                        "Productor", "email", email);
                });
            log.info("Found Productor by email: productorId={}", productor.getId());
        }
        Long productorId = productor.getId();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFincas",
            fincaRepository.findByProductorIdAndActivoTrue(
                productorId,
                org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements());
        stats.put("cultivosActivos",
            cultivoRepository.findByProductorId(
                productorId,
                org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements());
        stats.put("recomendacionesPendientes",
            countFromRecommendationsService(
                "http://localhost:8083/api/recomendaciones/pendientes"));
        stats.put("alertasActivas",
            countFromRecommendationsService(
                "http://localhost:8083/api/alertas"));
        stats.put("syncPendientes",
            syncRepository.findByUsuarioIdAndEstado(
                productor.getUsuarioId(),
                EstadoSincronizacion.PENDIENTE).size());

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAsociacionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productoresAsociados", productorRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        stats.put("cultivosPorZonaActivos", cultivoRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        stats.put("alertasRelevantes", countFromRecommendationsService("http://localhost:8083/api/alertas"));
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // Conteos globales del sistema
            stats.put("totalUsuarios", countFromAuthService("http://localhost:8081/api/admin/usuarios/count"));
            stats.put("productores", countFromAuthService("http://localhost:8081/api/admin/usuarios/count-by-role/AGRICULTOR"));
            stats.put("tecnicos", countFromAuthService("http://localhost:8081/api/admin/usuarios/count-by-role/TECNICO"));
            stats.put("asociaciones", countFromAuthService("http://localhost:8081/api/admin/usuarios/count-by-role/ASOCIACION"));
            stats.put("fincasRegistradas", fincaRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
            stats.put("cultivosRegistrados", cultivoRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
            stats.put("alertasActivas", countFromRecommendationsService("http://localhost:8083/api/alertas"));
        } catch (Exception e) {
            log.error("Error fetching admin stats", e);
            // Retornar valores por defecto si hay error
            stats.put("totalUsuarios", 0);
            stats.put("productores", 0);
            stats.put("tecnicos", 0);
            stats.put("asociaciones", 0);
            try {
                stats.put("fincasRegistradas", fincaRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
            } catch (Exception ex) {
                stats.put("fincasRegistradas", 0);
            }
            try {
                stats.put("cultivosRegistrados", cultivoRepository.findByActivoTrue(org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
            } catch (Exception ex) {
                stats.put("cultivosRegistrados", 0);
            }
            stats.put("alertasActivas", 0);
        }
        return stats;
    }

    @SuppressWarnings("unchecked")
    private long countFromAuthService(String url) {
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return 0L;
            Object datos = response.get("datos");
            if (datos instanceof Number number) return number.longValue();
            if (datos instanceof Map<?, ?> m && m.containsKey("count")) {
                Object count = m.get("count");
                if (count instanceof Number number) return number.longValue();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("Error calling auth service: {}", url, e);
            return 0L;
        }
    }

    @SuppressWarnings("unchecked")
    private long countFromRecommendationsService(String url) {
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            return 0L;
        }
        Object datosObj = response.get("datos");
        if (!(datosObj instanceof Map<?, ?> datos)) {
            return 0L;
        }
        Object totalElements = datos.get("totalElements");
        if (totalElements instanceof Number number) {
            return number.longValue();
        }
        Object content = datos.get("content");
        if (content instanceof List<?> list) {
            return list.size();
        }
        return 0L;
    }
}
