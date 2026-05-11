package com.agrosmart.magdalena.controller;

import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.repository.ProductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/productores")
@RequiredArgsConstructor
public class ProductorController {

    private final ProductorRepository productorRepository;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Long>> obtenerProductorPorUsuarioId(@PathVariable Long usuarioId) {
        return productorRepository.findByUsuarioId(usuarioId)
                .map(this::toResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/crear-desde-auth")
    public ResponseEntity<Map<String, Long>> crearDesdeAuth(@RequestBody ProductorDesdeAuthRequest request) {
        Productor productor = productorRepository.findByUsuarioId(request.usuarioId())
                .orElseGet(() -> {
                    String cedula = (request.cedula() != null && !request.cedula().isBlank()) 
                            ? request.cedula() 
                            : "PENDIENTE-" + request.usuarioId();
                    Productor nuevo = Productor.builder()
                            .usuarioId(request.usuarioId())
                            .nombreCompleto(request.nombreCompleto())
                            .email(request.email())
                            .cedula(cedula)
                            .telefono(request.telefono())
                            .activo(true)
                            .build();
                    return productorRepository.save(nuevo);
                });

        return toResponse(productor);
    }

    private ResponseEntity<Map<String, Long>> toResponse(Productor productor) {
        return ResponseEntity.ok(Map.of("productorId", productor.getId()));
    }

    public record ProductorDesdeAuthRequest(
            Long usuarioId,
            String nombreCompleto,
            String email,
            String telefono,
            String cedula
    ) {
    }
}