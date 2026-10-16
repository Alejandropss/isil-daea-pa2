package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.IncidenteConciliacion;
import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Guarda el incidente en su propia transacción (REQUIRES_NEW) para que no se pierda con el rollback. */
@Service
public class IncidenteConciliacionService {

  public static final String COMPENSACION_RESERVA = "COMPENSACION_RESERVA";

  private final IncidenteConciliacionRepository incidenteRepository;

  public IncidenteConciliacionService(IncidenteConciliacionRepository incidenteRepository) {
    this.incidenteRepository = incidenteRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public IncidenteConciliacion registrarCompensacionPendiente(String productoCodigo, int cantidad,
      String detalle) {
    return incidenteRepository.save(
        new IncidenteConciliacion(COMPENSACION_RESERVA, productoCodigo, cantidad, detalle));
  }
}
