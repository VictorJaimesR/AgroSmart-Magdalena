package com.agrosmart.magdalena.dto.response;

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
    private String nombreCompleto;
    private String email;
    private List<String> roles;

    public static AuthResponse of(String token, Long id, String nombre, String email, List<String> roles) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .usuarioId(id)
                .nombreCompleto(nombre)
                .email(email)
                .roles(roles)
                .build();
    }
}
