package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.IncidenteConciliacion;
import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra incidentes de conciliación en una transacción PROPIA.
 *
 * <p>REQUIRES_NEW suspende la transacción del pedido (que va a hacer rollback)
 * y abre otra: así el incidente queda guardado aunque el pedido se revierta.
 * Está en un bean separado porque Spring solo aplica @Transactional cuando la
 * llamada pasa por el proxy, no en llamadas internas de la misma clase.</p>
 */
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
