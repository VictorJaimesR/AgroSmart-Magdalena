package com.campovital.recommendations.integration.weather;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockWeatherServiceClient implements WeatherServiceClient {
    @Override
    public String obtenerResumenClima(String municipio) {
        return "Sin datos meteorológicos externos para " + municipio;
    }
}
