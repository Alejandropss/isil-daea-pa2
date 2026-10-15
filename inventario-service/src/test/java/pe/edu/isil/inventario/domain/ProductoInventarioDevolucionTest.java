package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pruebas PA2 de la operación de dominio ProductoInventario.devolverStock(...).
 * No necesitan WildFly ni SQL Server: prueban la entidad en memoria.
 */
class ProductoInventarioDevolucionTest {

  @Test
  @DisplayName("Devolución válida: incrementa el stock exactamente en la cantidad (8 + 2 = 10)")
  void devolucionValidaIncrementaElStock() {
    // Preparación
    ProductoInventario producto = nuevoProductoConStock(8);

    // Ejecución
    producto.devolverStock(2);

    // Verificación
    assertEquals(10, producto.getStock());
  }

  @ParameterizedTest(name = "Devolución inválida de {0}: se rechaza y el stock sigue en 8")
  @ValueSource(ints = {0, -3})
  void devolucionInvalidaSeRechazaSinCambiarElStock(int cantidadInvalida) {
    // Preparación
    ProductoInventario producto = nuevoProductoConStock(8);

    // Ejecución y verificación
    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class,
        () -> producto.devolverStock(cantidadInvalida)
    );
    assertEquals("La cantidad a devolver debe ser mayor que cero.", error.getMessage());
    assertEquals(8, producto.getStock());
  }

  private static ProductoInventario nuevoProductoConStock(int stock) {
    return new ProductoInventario("LAP-001", "Laptop", new BigDecimal("2500.00"), stock);
  }
}
