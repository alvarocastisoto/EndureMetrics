package com.alvaro.garmin.service;

import com.alvaro.garmin.entity.Usuario;
import com.alvaro.garmin.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UsuarioRepository usuarioRepository;

    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // 1. Programación defensiva extrema
        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario no pueden ser nulos.");
        }
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        // 2. Reglas de negocio (Evitar duplicados)
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso. Elige otro.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Este correo electrónico ya está registrado.");
        }

        // 3. TODO para el futuro: Aquí hashearíamos la contraseña antes de guardar
        String hash = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt(12));
        usuario.setPassword(hash);

        // 4. Guardar y devolver la entidad con su nuevo UUID
        return usuarioRepository.save(usuario);
    }

}