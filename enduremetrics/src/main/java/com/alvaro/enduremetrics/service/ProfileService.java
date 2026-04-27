package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.ProfileDTO;
import com.alvaro.enduremetrics.repository.UsuarioRepository;
import com.alvaro.enduremetrics.session.UserSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alvaro.enduremetrics.entity.Usuario;


@Service
public class ProfileService {

    private UsuarioRepository usuarioRepository;

    private UserSession userSession;

    public ProfileService(UsuarioRepository usuarioRepository, UserSession userSession) {
        this.usuarioRepository = usuarioRepository;
        this.userSession = userSession;
    }

    // En tu ProfileService.java
    public ProfileDTO obtenerPerfil(Usuario usuario) {
        // 1. Recargamos al usuario fresco desde la base de datos
        Usuario usuarioFresco = usuarioRepository.findByUsername(usuario.getUsername())
                .orElse(usuario);

        // 2. Mapeamos con los datos reales y actualizados
        return new ProfileDTO(
                usuarioFresco.getUsername(),
                usuarioFresco.getAltura(),
                usuarioFresco.getFechaNacimiento(),
                usuarioFresco.getSexo(),
                usuarioFresco.getPeso(),
                usuarioFresco.getFcMax(),
                usuarioFresco.getFcReposo()
        );
    }


    @Transactional
    public void actualizarPerfil(Usuario usuarioSession, ProfileDTO dto) {
        // 1. Cargamos el usuario de la BBDD
        Usuario usuario = usuarioRepository.findByUsername(usuarioSession.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Actualizamos todos los campos (¡AQUÍ FALTABAN LAS FC!)
        usuario.setAltura(dto.altura());
        usuario.setPeso(dto.peso());
        usuario.setFechaNacimiento(dto.fechaNacimiento());
        usuario.setSexo(dto.sexo());

        // --- LOS DOS SETTERS QUE TE FALTABAN ---
        usuario.setFcMax(dto.fcMax());
        usuario.setFcReposo(dto.fcReposo());

        // 3. Guardamos en BD y refrescamos la sesión
        usuarioRepository.save(usuario);
        userSession.setUsuarioLogueado(usuario);
    }
}
