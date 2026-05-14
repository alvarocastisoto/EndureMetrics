package com.alvaro.enduremetrics.service;

import com.alvaro.enduremetrics.entity.MetricaCorporal;
import com.alvaro.enduremetrics.entity.Usuario;
import com.alvaro.enduremetrics.repository.MetricaCorporalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ComposicionService {
    /**
     * Calcula el % de grasa corporal usando el método US Navy.
     *
     * @param sexo      "hombre" o "mujer" (extraído del Usuario)
     * @param alturaCm  Altura total en centímetros (extraído del Usuario)
     * @param cuelloCm  Perímetro del cuello
     * @param cinturaCm Perímetro de la cintura (a la altura del ombligo)
     * @param caderaCm  Perímetro de la cadera (parte más ancha, solo relevante para mujeres, pasar 0 si es hombre)
     * @return Porcentaje de grasa corporal estimado
     */

    private MetricaCorporalRepository metricaCorporalRepository;

    public ComposicionService(MetricaCorporalRepository metricaCorporalRepository) {
        this.metricaCorporalRepository = metricaCorporalRepository;
    }

    public void registrarMetricas(Usuario usuario, double peso, double cuello, double cintura, double cadera) {
        Double porcentajeGrasa = calcularGrasaUSNavy(
                usuario.getSexo(),
                usuario.getAltura(),
                cuello,
                cintura,
                cadera
        );
        Optional<MetricaCorporal> metricaHoy = metricaCorporalRepository.findByUsuarioAndFecha(usuario, LocalDate.now());
        MetricaCorporal nuevaMetrica = new MetricaCorporal();

        if(metricaHoy.isPresent()){
            nuevaMetrica = metricaHoy.get();
        }else{
            nuevaMetrica = new MetricaCorporal();
            nuevaMetrica.setUsuario(usuario);
            nuevaMetrica.setFecha(LocalDate.now());
        }
        nuevaMetrica.setPeso(peso);
        nuevaMetrica.setCuello(cuello);
        nuevaMetrica.setCintura(cintura);
        nuevaMetrica.setCadera(cadera);
        nuevaMetrica.setPorcentajeGrasa(porcentajeGrasa);

        metricaCorporalRepository.save(nuevaMetrica);
    }


    public List<MetricaCorporal> obtenerHistoricoReciente(Usuario usuario){
        LocalDate fechaLimite = LocalDate.now().minusMonths(3);
        return metricaCorporalRepository.findByUsuarioAndFechaAfterOrderByFechaAsc(usuario, fechaLimite);
    }


    private Double calcularGrasaUSNavy(String sexo, double alturaCm, double cuelloCm, double cinturaCm, double caderaCm) {
        if (alturaCm <= 0 || cuelloCm <= 0 || cinturaCm <= 0) {
            return null;
        }

        try {
            if (sexo != null && sexo.equalsIgnoreCase("mujer")) {
                if (caderaCm <= 0) return null;
                double logMedidas = Math.log10(cinturaCm + caderaCm - cuelloCm);
                double logAltura = Math.log10(alturaCm);
                return 495 / (1.29579 - 0.35004 * logMedidas + 0.22100 * logAltura) - 450;
            } else {
                double logMedidas = Math.log10(cinturaCm - cuelloCm);
                double logAltura = Math.log10(alturaCm);
                return 495.0 / (1.0324 - 0.19077 * logMedidas + 0.15456 * logAltura) - 450.0;
            }
        }catch (Exception e){
            System.out.println("Error matemático en el cáclulo de grasa: " + e.getMessage());
            return null;
        }
    }



}
