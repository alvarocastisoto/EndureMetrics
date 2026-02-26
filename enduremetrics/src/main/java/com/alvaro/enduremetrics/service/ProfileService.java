package com.alvaro.enduremetrics.service;

import java.util.UUID;

import com.alvaro.enduremetrics.dto.ProfileDTO;
import com.garmin.fit.Profile;
import org.springframework.stereotype.Service;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
public class ProfileService {
    public ProfileDTO obtenerPerfil(Usuario usuario){
        return new ProfileDTO(
                usuario.getUsername(),
                usuario.getAltura(),
                usuario.getFechaNacimiento(),
                usuario.getSexo()
        );
    }
}
