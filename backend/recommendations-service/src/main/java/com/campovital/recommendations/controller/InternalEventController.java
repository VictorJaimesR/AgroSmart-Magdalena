package com.campovital.recommendations.controller;

import com.campovital.recommendations.dto.request.CultivoStateChangedRequest;
import com.campovital.recommendations.dto.response.ApiResponse;
import com.campovital.recommendations.service.RecomendacionEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalEventController {

    private final RecomendacionEngineService recomendacionEngineService;

    @PostMapping("/cultivo-state-changed")
    public ResponseEntity<ApiResponse<Void>> cultivoStateChanged(@RequestBody CultivoStateChangedRequest request) {
        recomendacionEngineService.generarRecomendacionesAutomaticas(request.getCultivoId(), request.getNuevoEstado());
        return ResponseEntity.ok(ApiResponse.ok("Evento procesado", null));
    }
}
