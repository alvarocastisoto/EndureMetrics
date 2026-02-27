package com.alvaro.enduremetrics.repository;

import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.Zapatilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZapatillasRepository extends JpaRepository<Zapatilla, UUID> {

    // Método personalizado: Para que un usuario solo vea SUS zapatillas y no las de todos
    List<Zapatilla> findByUsuario(Usuario usuario);

    // Si quieres buscar zapatillas activas (que no estén jubiladas)
    List<Zapatilla> findByUsuarioAndActiva(Usuario usuario, Boolean activa);
}
