package com.campovital.auth.service;

import com.campovital.auth.domain.entity.Usuario;
import com.campovital.auth.domain.enums.EstadoUsuario;
import com.campovital.auth.dto.response.UsuarioAdminResponse;
import com.campovital.auth.exception.BadRequestException;
import com.campovital.auth.exception.ResourceNotFoundException;
import com.campovital.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioAdminResponse obtenerUsuario(Long id) {
        return toResponse(obtenerEntity(id));
    }

    @Transactional
    public UsuarioAdminResponse bloquearUsuario(Long id) {
        Usuario usuario = obtenerEntity(id);
        validarNoEsMismoUsuario(usuario);
        usuario.setEstado(EstadoUsuario.BLOQUEADO);
        usuario.setActivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse desbloquearUsuario(Long id) {
        Usuario usuario = obtenerEntity(id);
        validarNoEsMismoUsuario(usuario);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setActivo(true);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioAdminResponse eliminarUsuario(Long id) {
        Usuario usuario = obtenerEntity(id);
        validarNoEsMismoUsuario(usuario);
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuario.setActivo(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario obtenerEntity(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    private void validarNoEsMismoUsuario(Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return;
        }

        usuarioRepository.findByEmail(authentication.getName())
                .ifPresent(actual -> {
                    if (actual.getId().equals(usuario.getId())) {
                        throw new BadRequestException("No puedes modificar tu propio usuario");
                    }
                });
    }

    private UsuarioAdminResponse toResponse(Usuario usuario) {
        String rol = usuario.getRoles() == null || usuario.getRoles().isEmpty()
                ? null
                : usuario.getRoles().stream()
                .map(r -> r.getNombre().name())
                .sorted()
                .findFirst()
                .orElse(null);

        return UsuarioAdminResponse.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(rol)
                .telefono(usuario.getTelefono())
                .estado(usuario.getEstadoActual().name())
                .activo(Boolean.TRUE.equals(usuario.getActivo()))
                .fechaCreacion(usuario.getCreatedAt())
                .ultimoAcceso(null)
                .build();
    }

    @Transactional(readOnly = true)
    public Long totalUsuarios() {
        try {
            return usuarioRepository.countByActivoTrue();
        } catch (Exception e) {
            return usuarioRepository.count();
        }
    }

    @Transactional(readOnly = true)
    public Long contarPorRol(String rolNombre) {
        try {
            return usuarioRepository.countByRol(rolNombre);
        } catch (Exception e) {
            return 0L;
        }
    }
}
