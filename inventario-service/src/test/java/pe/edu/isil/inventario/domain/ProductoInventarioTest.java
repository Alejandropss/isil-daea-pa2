package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductoInventarioTest {

  @Test
  void debeDescontarStock() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        5
    );

    producto.reservarStock(2);

    assertEquals(3, producto.getStock());
    assertEquals(new BigDecimal("2500.00"), producto.getPrecio());
  }

  @Test
  void noDebePermitirReservarMasDelStockDisponible() {
    ProductoInventario producto = new ProductoInventario(
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        1
    );

    assertThrows(IllegalStateException.class, () -> producto.reservarStock(2));
    assertEquals(1, producto.getStock());
  }
}
