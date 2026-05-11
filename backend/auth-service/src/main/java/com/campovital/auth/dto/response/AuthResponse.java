package com.campovital.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo;
    private Long usuarioId;
    private Long productorId;
    private String nombreCompleto;
    private String email;
    private List<String> roles;

    public static AuthResponse of(String token, Long id, Long productorId, String nombre, String email, List<String> roles) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(id)
                .productorId(productorId)
                .nombreCompleto(nombre)
                .email(email)
                .roles(roles)
                .build();
    }
}
