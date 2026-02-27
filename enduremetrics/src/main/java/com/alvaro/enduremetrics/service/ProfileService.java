package com.alvaro.enduremetrics.service;

import java.util.UUID;

import com.alvaro.enduremetrics.dto.ProfileDTO;
import com.alvaro.enduremetrics.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alvaro.enduremetrics.entity.Usuario;

import lombok.RequiredArgsConstructor;

@Service
public class ProfileService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public ProfileDTO obtenerPerfil(Usuario usuario) {
        return new ProfileDTO(
                usuario.getUsername(),
                usuario.getAltura(),
                usuario.getFechaNacimiento(),
                usuario.getSexo(),
                usuario.getPeso()
        );
    }


    public Usuario actualizarPerfil(Usuario usuario, ProfileDTO profileDTO) {
        // 1. Volcamos los datos del Record DTO a la Entidad Usuario
        usuario.setAltura(profileDTO.altura());
        usuario.setPeso(profileDTO.peso());
        usuario.setFechaNacimiento(profileDTO.fechaNacimiento());
        usuario.setSexo(profileDTO.sexo());
        // El username no lo tocamos porque es el ID/natural y suele ser fijo

        // 2. Ahora sí, guardamos el objeto ya actualizado
        return usuarioRepository.save(usuario);
    }
}
