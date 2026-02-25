package com.alvaro.enduremetrics.service;

import org.springframework.stereotype.Service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.UsuarioRepository;

import org.mindrot.jbcrypt.BCrypt;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsuarioRepository usuarioRepository;

    public Usuario validarCredenciales(String username, String password) {

        // 1. Si no existe, lanzamos el error genérico
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos."));

        // 2. Si la clave está mal, lanzamos EXACTAMENTE el mismo error
        if (!BCrypt.checkpw(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        return usuario;
    }
}