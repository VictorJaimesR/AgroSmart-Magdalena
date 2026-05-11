package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.Productor;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.ProductorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de productores.
 */
@Service
@RequiredArgsConstructor
public class ProductorService {

    private final ProductorRepository productorRepository;

    @Transactional(readOnly = true)
    public Page<Productor> listar(Pageable pageable) {
        return productorRepository.findByActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Productor obtenerPorId(Long id) {
        return productorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Productor", "id", id));
    }

    @Transactional(readOnly = true)
    public Productor obtenerPorUsuarioId(Long usuarioId) {
        return productorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Productor", "usuarioId", usuarioId));
    }

    @Transactional(readOnly = true)
    public Page<Productor> listarPorAsociacion(Long asociacionId, Pageable pageable) {
        return productorRepository.findByAsociacionId(asociacionId, pageable);
    }
}
