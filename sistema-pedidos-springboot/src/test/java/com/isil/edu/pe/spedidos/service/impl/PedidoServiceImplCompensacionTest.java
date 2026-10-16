package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.IncidenteConciliacionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reto: pruebas de la compensación (el fallo se activa con true en el constructor). */
class PedidoServiceImplCompensacionTest {

  private PedidoRepository pedidoRepository;
  private InventarioClient inventarioClient;
  private DevolucionStockClient devolucionStockClient;
  private IncidenteConciliacionService incidenteService;
  private PedidoServiceImpl service;

  @BeforeEach
  void setUp() {
    pedidoRepository = mock(PedidoRepository.class);
    inventarioClient = mock(InventarioClient.class);
    devolucionStockClient = mock(DevolucionStockClient.class);
    incidenteService = mock(IncidenteConciliacionService.class);
    service = new PedidoServiceImpl(pedidoRepository, inventarioClient, devolucionStockClient,
        incidenteService, true);

    when(inventarioClient.reservarStock("LAP-001", 2)).thenReturn(reserva());
    when(pedidoRepository.save(any(Pedido.class)))
        .thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void falloLocalTrasReservarCompensaDevolviendoElStock() {
    PedidoException error = assertThrows(PedidoException.class,
        () -> service.registrarPedido("Ana Torres", "LAP-001", 2));

    // Se devuelve exactamente lo que se reservó; no hace falta incidente.
    verify(devolucionStockClient).devolverStock("LAP-001", 2);
    verifyNoInteractions(incidenteService);
    assertTrue(error.getMessage().contains("compensación"));
  }

  @Test
  void siLaCompensacionFallaSeRegistraIncidentePendiente() {
    when(devolucionStockClient.devolverStock("LAP-001", 2))
        .thenThrow(new InventarioNoDisponibleException("Inventario detenido."));

    PedidoException error = assertThrows(PedidoException.class,
        () -> service.registrarPedido("Ana Torres", "LAP-001", 2));

    verify(incidenteService).registrarCompensacionPendiente(eq("LAP-001"), eq(2), anyString());
    assertTrue(error.getMessage().contains("incidente pendiente de conciliación"));
  }

  private static ReservaStockResponse reserva() {
    ReservaStockResponse reserva = new ReservaStockResponse();
    reserva.setCodigo("LAP-001");
    reserva.setNombre("Laptop");
    reserva.setPrecio(new BigDecimal("2500.00"));
    return reserva;
  }
}
