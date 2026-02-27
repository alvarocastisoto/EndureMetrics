package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.dto.ZapatillaDTO;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.entity.Zapatilla;
import com.alvaro.enduremetrics.repository.ZapatillasRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZapatillasService {

    // Dependencia inyectada por constructor y marcada como final
    private final ZapatillasRepository zapatillasRepository;

    public ZapatillasService(ZapatillasRepository zapatillasRepository) {
        this.zapatillasRepository = zapatillasRepository;
    }

    /**
     * Devuelve la lista de zapatillas del usuario.
     * Si no tiene ninguna, devuelve una lista vacía, nunca null.
     */
    public List<Zapatilla> obtenerZapatillas(Usuario usuario) {
        return zapatillasRepository.findByUsuario(usuario);
    }

    public Zapatilla añadirZapatilla(Usuario usuario, ZapatillaDTO dto) {
        Zapatilla nuevaZapatilla = new Zapatilla();
        nuevaZapatilla.setMarca(dto.marca());
        nuevaZapatilla.setModelo(dto.modelo());
        nuevaZapatilla.setKmActuales(dto.kmActuales());
        nuevaZapatilla.setKmMaximos(dto.kmMaximos());
        nuevaZapatilla.setTipoTerreno(dto.tipoTerreno());

        nuevaZapatilla.setActiva(dto.activa() != null ? dto.activa() : true);

        nuevaZapatilla.setUsuario(usuario);

        return zapatillasRepository.save(nuevaZapatilla);
    }

    public void eliminarZapatilla(Zapatilla zapatilla) {
        // JpaRepository ya trae el método delete() gratis
        zapatillasRepository.delete(zapatilla);
    }

}