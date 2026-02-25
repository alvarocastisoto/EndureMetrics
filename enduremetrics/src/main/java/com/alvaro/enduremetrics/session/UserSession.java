package com.alvaro.enduremetrics.session;

import org.springframework.stereotype.Component;

import com.alvaro.enduremetrics.entity.Usuario;

@Component
public class UserSession {
    private Usuario usuario;

    public void setUsuarioLogueado(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuarioLogueado() {
        return usuario;
    }

    public void cerrarSesion() {
        this.usuario = null;
    }

    public boolean haySesionActiva() {
        return this.usuario != null;
    }

}
