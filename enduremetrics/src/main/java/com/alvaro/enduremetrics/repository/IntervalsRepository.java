package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.IntervalsCredentials;
import com.alvaro.enduremetrics.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntervalsRepository extends JpaRepository<IntervalsCredentials, UUID> {
    Optional<IntervalsCredentials> findByUsuario(Usuario usuario);
}
