package com.campovital.recommendations.integration.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente real que consulta Open-Meteo (gratuito, sin API key).
 * Documentación: https://open-meteo.com/en/docs
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenMeteoWeatherClient implements WeatherServiceClient {

    private final RestTemplate restTemplate;

    // Municipios clave del Magdalena con sus coordenadas
    public static final Map<String, double[]> MUNICIPIOS_MAGDALENA = Map.ofEntries(
            Map.entry("Santa Marta", new double[] { 11.2408, -74.1990 }),
            Map.entry("Ciénaga", new double[] { 11.0000, -74.2500 }),
            Map.entry("Fundación", new double[] { 10.5197, -74.1869 }),
            Map.entry("Aracataca", new double[] { 10.5933, -74.1897 }),
            Map.entry("El Banco", new double[] { 9.0000, -73.9833 }),
            Map.entry("Plato", new double[] { 9.7992, -74.7858 }),
            Map.entry("Pivijay", new double[] { 10.4667, -74.6167 }),
            Map.entry("Zona Bananera", new double[] { 10.8167, -74.1833 }));

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast" +
            "?latitude={lat}&longitude={lon}" +
            "&current=temperature_2m,precipitation,windspeed_10m,weathercode" +
            "&hourly=precipitation_probability" +
            "&forecast_days=1" +
            "&timezone=auto";

    @Override
    public String obtenerResumenClima(String municipio) {
        double[] coords = MUNICIPIOS_MAGDALENA.getOrDefault(municipio,
                MUNICIPIOS_MAGDALENA.get("Santa Marta"));
        try {
            WeatherData data = fetchWeatherData(coords[0], coords[1]);
            return String.format("%s — Temp: %.1f°C | Lluvia: %.1fmm | Viento: %.1fkm/h | Código: %d",
                    municipio, data.temperatura(), data.precipitacion(),
                    data.viento(), data.weatherCode());
        } catch (Exception e) {
            log.error("Error consultando Open-Meteo para {}: {}", municipio, e.getMessage());
            return "Sin datos para " + municipio;
        }
    }

    /**
     * Obtiene datos climáticos actuales para unas coordenadas dadas.
     */
    public WeatherData fetchWeatherData(double lat, double lon) {
        try {
            Map response = restTemplate.getForObject(
                    OPEN_METEO_URL, Map.class,
                    lat, lon);

            if (response == null)
                throw new RuntimeException("Respuesta vacía de Open-Meteo");

            Map<String, Object> current = (Map<String, Object>) response.get("current");
            if (current == null)
                throw new RuntimeException("Sin datos 'current' en respuesta");

            double temperatura = toDouble(current.get("temperature_2m"));
            double precipitacion = toDouble(current.get("precipitation"));
            double viento = toDouble(current.get("windspeed_10m"));
            int weatherCode = toInt(current.get("weathercode"));

            return new WeatherData(temperatura, precipitacion, viento, weatherCode);

        } catch (Exception e) {
            log.error("Error en Open-Meteo lat={} lon={}: {}", lat, lon, e.getMessage());
            throw new RuntimeException("Error consultando clima: " + e.getMessage(), e);
        }
    }

    private double toDouble(Object val) {
        if (val == null)
            return 0.0;
        if (val instanceof Number)
            return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    private int toInt(Object val) {
        if (val == null)
            return 0;
        if (val instanceof Number)
            return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    /**
     * Datos climáticos actuales de un punto geográfico.
     *
     * @param temperatura   en grados Celsius
     * @param precipitacion en mm
     * @param viento        en km/h
     * @param weatherCode   código WMO (ver
     *                      https://open-meteo.com/en/docs#weathervariables)
     */
    public record WeatherData(double temperatura, double precipitacion, double viento, int weatherCode) {

        /** Lluvia intensa: > 10mm actuales o código WMO de lluvia fuerte/tormenta */
        public boolean esLluviaIntensa() {
            return precipitacion > 10.0 || (weatherCode >= 61 && weatherCode <= 67)
                    || (weatherCode >= 80 && weatherCode <= 82);
        }

        /** Tormenta eléctrica: códigos WMO 95–99 */
        public boolean esTormenta() {
            return weatherCode >= 95 && weatherCode <= 99;
        }

        /** Calor extremo para el Magdalena: > 38°C */
        public boolean esCalorExtremo() {
            return temperatura > 38.0;
        }

        /** Viento fuerte: > 50 km/h */
        public boolean esVientoFuerte() {
            return viento > 50.0;
        }

        /** Sequía potencial: temperatura alta + sin lluvia */
        public boolean esSequia() {
            return temperatura > 35.0 && precipitacion == 0.0;
        }
    }
}