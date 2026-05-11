package com.campovital.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import com.campovital.auth.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.campovital.auth.domain.entity.Usuario;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor de tokens JWT.
 * Genera, valida y extrae información de los tokens de acceso.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private final UsuarioRepository usuarioRepository;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT a partir de la autenticación.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // Extract first authority as role (e.g., ROLE_AGRICULTOR) and strip prefix for claim
        String role = "";
        if (!userDetails.getAuthorities().isEmpty()) {
            role = userDetails.getAuthorities().iterator().next().getAuthority();
            if (role != null && role.startsWith("ROLE_")) role = role.substring(5);
        }

        String usuarioId = usuarioRepository.findByEmail(userDetails.getUsername())
            .map(usuario -> usuario.getId() != null ? usuario.getId().toString() : null)
            .orElse(null);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
            .claim("usuarioId", usuarioId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera un token JWT directamente desde el email del usuario.
     */
    public String generateTokenFromEmail(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera un token JWT a partir de la entidad Usuario, incluyendo su rol principal.
     */
    public String generateTokenFromUsuario(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String role = "";
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            String rn = usuario.getRoles().iterator().next().getNombre().name();
            role = rn != null && rn.startsWith("ROLE_") ? rn.substring(5) : rn;
        }

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("role", role)
            .claim("usuarioId", usuario.getId() != null ? usuario.getId().toString() : null)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrae el email (subject) del token JWT.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Valida la integridad y vigencia del token JWT.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
