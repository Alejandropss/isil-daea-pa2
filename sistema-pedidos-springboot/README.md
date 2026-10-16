# Sistema de Pedidos - Spring Boot (PA2)

Proyecto de pedidos en Spring Boot que se conecta con `inventario-service` (WildFly). En la PA2 agregué la **anulación de pedidos**: el stock vuelve a Inventario y el pedido queda como `ANULADO`.

![Arquitectura PA2](../docs/arquitectura-pa2.png)

## Requisitos

- JDK 21 y Maven
- `inventario-service` corriendo en el puerto 8082
- SQL Server con la base `InventarioDB` (la usa Inventario)

## Cómo ejecutar

Orden: **SQL Server → WildFly → Spring Boot**

```bash
# en inventario-service
mvn clean wildfly:run
# en sistema-pedidos-springboot
mvn spring-boot:run
```

- Pantalla: http://localhost:8081/sistema-pedidos/pedidos
- Consola H2: http://localhost:8081/sistema-pedidos/h2-console (`jdbc:h2:mem:pedidosdb`)

## Endpoints nuevos

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/sistema-pedidos/api/v1/anulaciones` | Anula un pedido y devuelve el stock |
| GET | `/sistema-pedidos/api/v1/anulaciones` | Lista las anulaciones |

`GET /api/v1/orders` ahora también muestra el `estado` del pedido.

Ejemplo:

```json
{ "pedidoId": 1, "motivo": "Solicitud del cliente" }
```

Respuesta `201`:

```json
{ "id": 1, "pedidoId": 1, "productoCodigo": "LAP-001", "cantidadDevuelta": 2,
  "motivo": "Solicitud del cliente", "estadoPedido": "ANULADO" }
```

Errores: `404` pedido no existe, `409` ya estaba anulado, `503` Inventario apagado, `400` datos inválidos.

## Configuración

En `application.properties` (no hay contraseñas):

```properties
inventario.api.base-url=http://localhost:8082/inventario-service/api/inventario
pedidos.simulacion.fallo-local-tras-reserva=false
```

## Reto (compensación)

Si después de reservar stock falla algo en Spring antes de guardar en H2, se devuelve el stock a Inventario y el pedido no se guarda. Para probarlo se activa la simulación al arrancar:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--pedidos.simulacion.fallo-local-tras-reserva=true"
```

## Pruebas

```bash
mvn test
```

11 pruebas, 0 fallos. Las nuevas son `AnulacionPedidoServiceImplTest` (3), `PedidoServiceImplCompensacionTest` (2) y `ReservaCompensadaIntegrationTest` (1).

## Reproducir los escenarios

En `../scripts/demo-pa2/` hay un script por escenario (orden 1, 2, 3, 5 y 4, porque el 4 apaga WildFly):

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\demo-pa2\escenario1-anulacion-correcta.ps1
```

También están las peticiones en `requests/anulaciones.http`.

## Limitaciones

- No hay transacción distribuida entre H2 y SQL Server.
- H2 está en memoria: los datos se borran al reiniciar.
- La devolución no evita peticiones repetidas, por eso no se reintenta.
