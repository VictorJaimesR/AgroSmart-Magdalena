package com.campovital.auth.config;

import com.campovital.auth.domain.entity.Rol;
import com.campovital.auth.domain.enums.RolNombre;
import com.campovital.auth.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleSeeder {

    @Bean
    public CommandLineRunner initRoles(RolRepository rolRepository) {
        return args -> {
            for (RolNombre rolNombre : RolNombre.values()) {
                if (rolRepository.findByNombre(rolNombre).isEmpty()) {
                    Rol rol = new Rol();
                    rol.setNombre(rolNombre);
                    rolRepository.save(rol);
                }
            }
        };
    }
}
