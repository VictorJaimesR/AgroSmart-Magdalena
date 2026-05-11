package com.agrosmart.magdalena.repository;

import com.agrosmart.magdalena.domain.entity.SincronizacionOffline;
import com.agrosmart.magdalena.domain.enums.EstadoSincronizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SincronizacionOfflineRepository extends JpaRepository<SincronizacionOffline, Long> {

    List<SincronizacionOffline> findByUsuarioIdAndEstado(Long usuarioId, EstadoSincronizacion estado);

    Page<SincronizacionOffline> findByUsuarioId(Long usuarioId, Pageable pageable);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoSincronizacion estado);
}
