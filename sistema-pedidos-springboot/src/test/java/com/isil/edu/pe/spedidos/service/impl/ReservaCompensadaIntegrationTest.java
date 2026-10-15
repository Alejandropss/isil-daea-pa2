package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClient;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.client.dto.ReservaStockResponse;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.IncidenteConciliacionRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.PedidoService;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * PA2 - Reto, escenario completo con Spring y H2 reales (Inventario simulado):
 * stock 10 -> reserva 2 (queda 8) -> fallo local antes del commit
 * -> compensación (vuelve a 10) y el pedido NO queda en H2.
 *
 * <p>La simulación se activa solo en esta prueba mediante la propiedad;
 * en application.properties sigue en false.</p>
 */
@SpringBootTest(properties = "pedidos.simulacion.fallo-local-tras-reserva=true")
class ReservaCompensadaIntegrationTest {

  @Autowired
  private PedidoService pedidoService;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private IncidenteConciliacionRepository incidenteRepository;

  @MockitoBean
  private InventarioClient inventarioClient;

  @MockitoBean
  private DevolucionStockClient devolucionStockClient;

  @Test
  void falloLocalTrasReservarRestauraElStockYNoGuardaElPedido() {
    AtomicInteger stockSimulado = new AtomicInteger(10);

    when(inventarioClient.reservarStock(anyString(), anyInt())).thenAnswer(inv -> {
      int cantidad = inv.getArgument(1);
      stockSimulado.addAndGet(-cantidad);
      ReservaStockResponse reserva = new ReservaStockResponse();
      reserva.setCodigo(inv.getArgument(0));
      reserva.setNombre("Laptop");
      reserva.setPrecio(new BigDecimal("2500.00"));
      return reserva;
    });
    when(devolucionStockClient.devolverStock(anyString(), anyInt())).thenAnswer(inv -> {
      int cantidad = inv.getArgument(1);
      DevolucionStockResponse devolucion = new DevolucionStockResponse();
      devolucion.setCodigo(inv.getArgument(0));
      devolucion.setCantidadDevuelta(cantidad);
      devolucion.setStockRestante(stockSimulado.addAndGet(cantidad));
      return devolucion;
    });

    long pedidosAntes = pedidoRepository.count();

    assertThrows(PedidoException.class, () -> pedidoService.registrarPedido("Ana Torres", "LAP-001", 2));

    assertEquals(10, stockSimulado.get(), "La compensación debe restaurar el stock a 10.");
    assertEquals(pedidosAntes, pedidoRepository.count(), "El rollback de H2 no debe dejar el pedido guardado.");
    assertEquals(0, incidenteRepository.count(), "Si la compensación funciona no hay incidente.");
  }
}
