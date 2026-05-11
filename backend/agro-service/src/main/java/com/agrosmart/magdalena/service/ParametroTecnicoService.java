package com.agrosmart.magdalena.service;

import com.agrosmart.magdalena.domain.entity.ParametroTecnico;
import com.agrosmart.magdalena.domain.entity.Usuario;
import com.agrosmart.magdalena.dto.request.ParametroTecnicoRequest;
import com.agrosmart.magdalena.dto.response.ParametroTecnicoResponse;
import com.agrosmart.magdalena.exception.ConflictException;
import com.agrosmart.magdalena.exception.ResourceNotFoundException;
import com.agrosmart.magdalena.repository.ParametroTecnicoRepository;
import com.agrosmart.magdalena.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParametroTecnicoService {

    private final ParametroTecnicoRepository parametroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ParametroTecnicoResponse> listarTodos() {
        return parametroRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ParametroTecnicoResponse> listarPorCategoria(String categoria) {
        return parametroRepository.findByCategoria(categoria).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ParametroTecnicoResponse obtenerPorClave(String clave) {
        ParametroTecnico p = parametroRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("ParametroTecnico", "clave", clave));
        return toResponse(p);
    }

    @Transactional
    public ParametroTecnicoResponse crear(ParametroTecnicoRequest request, String emailAdmin) {
        if (parametroRepository.existsByClave(request.getClave())) {
            throw new ConflictException("Ya existe un parámetro con clave: " + request.getClave());
        }
        Usuario admin = usuarioRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailAdmin));

        ParametroTecnico p = ParametroTecnico.builder()
                .clave(request.getClave()).valor(request.getValor())
                .descripcion(request.getDescripcion()).categoria(request.getCategoria())
                .modificadoPor(admin).build();
        p = parametroRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public ParametroTecnicoResponse actualizar(String clave, ParametroTecnicoRequest req, String emailAdmin) {
        ParametroTecnico p = parametroRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("ParametroTecnico", "clave", clave));
        Usuario admin = usuarioRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", emailAdmin));
        p.setValor(req.getValor());
        p.setDescripcion(req.getDescripcion());
        p.setCategoria(req.getCategoria());
        p.setModificadoPor(admin);
        p = parametroRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void eliminar(String clave) {
        ParametroTecnico p = parametroRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("ParametroTecnico", "clave", clave));
        parametroRepository.delete(p);
    }

    private ParametroTecnicoResponse toResponse(ParametroTecnico p) {
        return ParametroTecnicoResponse.builder()
                .id(p.getId()).clave(p.getClave()).valor(p.getValor())
                .descripcion(p.getDescripcion()).categoria(p.getCategoria())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .modificadoPor(p.getModificadoPor() != null ? p.getModificadoPor().getNombreCompleto() : null)
                .build();
    }
}
