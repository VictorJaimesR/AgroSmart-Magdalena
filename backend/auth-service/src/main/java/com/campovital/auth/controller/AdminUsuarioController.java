package com.campovital.auth.controller;

import com.campovital.auth.dto.response.ApiResponse;
import com.campovital.auth.dto.response.UsuarioAdminResponse;
import com.campovital.auth.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administración de usuarios", description = "Gestión administrativa de usuarios")
@RestController
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioAdminResponse>>> listarUsuarios() {
        return ResponseEntity.ok(ApiResponse.ok("Usuarios listados", adminUsuarioService.listarUsuarios()));
    }

    @Operation(summary = "Obtener usuario")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioAdminResponse>> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido", adminUsuarioService.obtenerUsuario(id)));
    }

    @Operation(summary = "Bloquear usuario")
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<ApiResponse<UsuarioAdminResponse>> bloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario bloqueado", adminUsuarioService.bloquearUsuario(id)));
    }

    @Operation(summary = "Desbloquear usuario")
    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse<UsuarioAdminResponse>> desbloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario desbloqueado", adminUsuarioService.desbloquearUsuario(id)));
    }

    @Operation(summary = "Desactivar usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioAdminResponse>> eliminarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario desactivado", adminUsuarioService.eliminarUsuario(id)));
    }

    @Operation(summary = "Total de usuarios")
    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Long>> totalUsuarios() {
        return ResponseEntity.ok(ApiResponse.ok("Total usuarios", adminUsuarioService.totalUsuarios()));
    }

    @Operation(summary = "Contar usuarios por rol")
    @GetMapping("/count-by-role/{rolNombre}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Long>> contarPorRol(@PathVariable String rolNombre) {
        return ResponseEntity.ok(ApiResponse.ok("Usuarios por rol", adminUsuarioService.contarPorRol(rolNombre)));
    }
}
