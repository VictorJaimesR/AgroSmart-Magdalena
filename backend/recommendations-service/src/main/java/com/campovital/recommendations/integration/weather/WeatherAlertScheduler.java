package com.campovital.recommendations.integration.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherAlertScheduler {

    private final WeatherServiceClient weatherServiceClient;

    @Scheduled(fixedDelay = 86400000L)
    public void revisarClima() {
        log.debug("WeatherAlertScheduler activo: {}", weatherServiceClient.obtenerResumenClima("Magdalena"));
    }
}
