package com.campovital.recommendations.integration.weather;

import com.campovital.recommendations.domain.entity.AlertaClimatica;
import com.campovital.recommendations.domain.enums.EstadoAlerta;
import com.campovital.recommendations.domain.enums.TipoAlerta;
import com.campovital.recommendations.repository.AlertaClimaticaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherAlertScheduler {

    private final OpenMeteoWeatherClient weatherClient;
    private final AlertaClimaticaRepository alertaRepository;

    /**
     * Revisa el clima cada 6 horas para todos los municipios del Magdalena.
     * También se ejecuta 30 segundos después de arrancar la aplicación.
     */
    @Scheduled(fixedDelay = 21_600_000L, initialDelay = 30_000L)
    public void revisarClima() {
        log.info("Iniciando revisión climática automática para el Magdalena...");
        int alertasCreadas = 0;

        for (Map.Entry<String, double[]> entry : OpenMeteoWeatherClient.MUNICIPIOS_MAGDALENA.entrySet()) {
            String municipio = entry.getKey();
            double[] coords = entry.getValue();

            try {
                OpenMeteoWeatherClient.WeatherData data = weatherClient.fetchWeatherData(coords[0], coords[1]);

                alertasCreadas += evaluarYCrearAlertas(municipio, data);

            } catch (Exception e) {
                log.warn("Error procesando clima para {}: {}", municipio, e.getMessage());
            }
        }

        log.info("Revisión climática completada. Alertas nuevas creadas: {}", alertasCreadas);
    }

    /**
     * Evalúa los datos y crea alertas si superan los umbrales.
     * Evita duplicar alertas del mismo tipo y municipio activas en las últimas 6
     * horas.
     */
    private int evaluarYCrearAlertas(String municipio, OpenMeteoWeatherClient.WeatherData data) {
        int count = 0;

        if (data.esTormenta()) {
            count += crearAlertaSiNoExiste(municipio, TipoAlerta.TORMENTA,
                    "⛈️ Tormenta eléctrica en " + municipio,
                    String.format("Se detectó actividad de tormenta eléctrica en %s. " +
                            "Temperatura: %.1f°C | Precipitación: %.1f mm | Viento: %.1f km/h. " +
                            "Evite trabajar al aire libre y resguarde equipos y animales.",
                            municipio, data.temperatura(), data.precipitacion(), data.viento()),
                    "🔴");
        }

        if (data.esLluviaIntensa() && !data.esTormenta()) {
            count += crearAlertaSiNoExiste(municipio, TipoAlerta.INUNDACION,
                    "🌧️ Lluvia intensa en " + municipio,
                    String.format("Se registra lluvia intensa en %s con %.1f mm de precipitación. " +
                            "Riesgo de encharcamiento e inundación en zonas bajas. " +
                            "Revise drenajes y proteja sus cultivos.",
                            municipio, data.precipitacion()),
                    "🟠");
        }

        if (data.esCalorExtremo()) {
            count += crearAlertaSiNoExiste(municipio, TipoAlerta.ONDA_DE_CALOR,
                    "🌡️ Calor extremo en " + municipio,
                    String.format("Temperatura extrema de %.1f°C registrada en %s. " +
                            "Se recomienda aumentar la frecuencia de riego, " +
                            "evitar labores agrícolas en horas pico (10am–3pm) y " +
                            "proteger cultivos sensibles con malla sombra.",
                            data.temperatura(), municipio),
                    "🟡");
        }

        if (data.esVientoFuerte()) {
            count += crearAlertaSiNoExiste(municipio, TipoAlerta.VIENTO_FUERTE,
                    "💨 Viento fuerte en " + municipio,
                    String.format("Vientos de %.1f km/h registrados en %s. " +
                            "Asegure estructuras, tutores de plantas y mallas. " +
                            "Riesgo de volcamiento en cultivos de porte alto.",
                            data.viento(), municipio),
                    "🟡");
        }

        if (data.esSequia()) {
            count += crearAlertaSiNoExiste(municipio, TipoAlerta.SEQUIA,
                    "☀️ Riesgo de sequía en " + municipio,
                    String.format("Temperatura de %.1f°C sin precipitaciones en %s. " +
                            "Se recomienda activar riego de emergencia, " +
                            "aplicar mulch para retener humedad del suelo y " +
                            "monitorear signos de estrés hídrico en cultivos.",
                            data.temperatura(), municipio),
                    "🟡");
        }

        return count;
    }

    private int crearAlertaSiNoExiste(String municipio, TipoAlerta tipo,
            String titulo, String descripcion, String nivel) {
        // No crear si ya existe una alerta activa del mismo tipo para este municipio
        // en las últimas 6 horas (evitar spam de alertas)
        LocalDateTime hace6Horas = LocalDateTime.now().minusHours(6);
        boolean yaExiste = alertaRepository
                .existeAlertaReciente(municipio, tipo, hace6Horas);

        if (yaExiste) {
            log.debug("Alerta {} en {} ya existe, omitiendo.", tipo, municipio);
            return 0;
        }

        AlertaClimatica alerta = AlertaClimatica.builder()
                .titulo(titulo)
                .descripcion(descripcion)
                .tipo(tipo)
                .fechaEmision(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusHours(12))
                .municipiosAfectados(List.of(municipio))
                .activa(true)
                .estadoAlerta(EstadoAlerta.NUEVA)
                .build();

        alertaRepository.save(alerta);
        log.info("{} Alerta creada: {} — {}", nivel, tipo, municipio);
        return 1;
    }
}