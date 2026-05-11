package com.campovital.auth.service;

import com.campovital.auth.domain.entity.*;
import com.campovital.auth.domain.enums.RolNombre;
import com.campovital.auth.dto.request.LoginRequest;
import com.campovital.auth.dto.request.RegisterRequest;
import com.campovital.auth.dto.response.AuthResponse;
import com.campovital.auth.exception.BadRequestException;
import com.campovital.auth.exception.ConflictException;
import com.campovital.auth.exception.ResourceNotFoundException;
import com.campovital.auth.repository.*;
import com.campovital.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación y registro.
 * Gestiona login con JWT y creación de usuarios con sus perfiles asociados.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RestTemplate restTemplate;

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.agro-service.url}")
    private String agroServiceUrl;

    /**
     * Autentica un usuario y genera un token JWT.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", request.getEmail()));

        Long productorId = obtenerProductorId(usuario.getId());

        return AuthResponse.of(
                token,
                usuario.getId(),
            productorId,
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRoles().stream()
                        .map(r -> r.getNombre().name())
                        .collect(Collectors.toList())
        );
    }

    /**
     * Registra un nuevo usuario con el rol especificado.
     * Si el rol es AGRICULTOR, crea también el perfil de AGRICULTOR.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado: " + request.getEmail());
        }

        // Determinar el rol
        RolNombre rolNombre = determinarRol(request.getRol());
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", rolNombre));

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .cedula(request.getCedula())
                .roles(Collections.singleton(rol))
                .build();
        usuario = usuarioRepository.save(usuario);

        Long productorId = crearPerfilProductor(usuario);
        if (productorId == null) {
            productorId = obtenerProductorId(usuario.getId());
        }

        // Generar token incluyendo rol en el claim
        String token = jwtTokenProvider.generateTokenFromUsuario(usuario);

        return AuthResponse.of(
                token,
                usuario.getId(),
                productorId,
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRoles().stream()
                        .map(r -> r.getNombre().name())
                        .collect(Collectors.toList())
        );
    }

    private Long crearPerfilProductor(Usuario usuario) {
        boolean esAgricultor = usuario.getRoles() != null && usuario.getRoles().stream()
                .anyMatch(rol -> RolNombre.ROLE_AGRICULTOR.equals(rol.getNombre()));
        if (!esAgricultor) {
            return null;
        }

        try {
            String url = agroServiceUrl + "/api/productores/crear-desde-auth";
            Map<String, Object> payload = new HashMap<>();
            payload.put("usuarioId", usuario.getId());
            payload.put("nombreCompleto", usuario.getNombreCompleto());
            payload.put("email", usuario.getEmail());
            payload.put("telefono", usuario.getTelefono());
            payload.put("cedula", usuario.getCedula());

            Map response = restTemplate.postForObject(url, payload, Map.class);
            if (response == null || response.get("productorId") == null) {
                return null;
            }

            Object productorId = response.get("productorId");
            if (productorId instanceof Number number) {
                return number.longValue();
            }
            return Long.valueOf(productorId.toString());
        } catch (RestClientException | NumberFormatException ex) {
            log.error("No se pudo crear/obtener el perfil de productor para usuarioId={}", usuario.getId(), ex);
            return null;
        }
    }

    private Long obtenerProductorId(Long usuarioId) {
        try {
            String url = agroServiceUrl + "/api/productores/usuario/" + usuarioId;
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("productorId") || response.get("productorId") == null) {
                return null;
            }

            Object productorId = response.get("productorId");
            if (productorId instanceof Number number) {
                return number.longValue();
            }
            return Long.valueOf(productorId.toString());
        } catch (RestClientException | NumberFormatException ex) {
            return null;
        }
    }

    private RolNombre determinarRol(String rolStr) {
        if (rolStr == null || rolStr.isBlank()) {
            return RolNombre.ROLE_AGRICULTOR; // Rol por defecto
        }
        try {
            String normalizado = rolStr.toUpperCase().startsWith("ROLE_") ? rolStr.toUpperCase() : "ROLE_" + rolStr.toUpperCase();
            return RolNombre.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido: " + rolStr);
        }
    }


}
