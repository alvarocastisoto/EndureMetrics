package com.alvaro.enduremetrics.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UsuarioRepository usuarioRepository;

    public void obtenerPerfil(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos."));
    }
}
