# Inventario Service — Jakarta EE 11 + SQL Server Express 2025

Proyecto académico complementario de `sistema-pedidos-jsp` para la Sesión 4: **Integración de aplicativos web con componentes empresariales**.

## Stack

- Java 21
- Maven
- Jakarta EE 11
- WildFly 41
- Jakarta REST
- Jakarta Enterprise Beans (EJB)
- Jakarta Persistence 3.2
- Jakarta Transactions (JTA)
- Bean Validation
- Microsoft JDBC Driver for SQL Server 13.6
- SQL Server Express 2025

## Arquitectura

```text
Sistema de Pedidos / Postman
          |
          | HTTP + JSON
          v
ProductoResource (Jakarta REST)
          |
          | @EJB
          v
InventarioService (@Stateless)
          |
          | @PersistenceContext
          v
EntityManager / InventarioPU
          |
          | java:app/jdbc/InventarioDS
          v
SQL Server Express 2025
```

## Preparación rápida

1. Habilitar TCP/IP en SQL Server Express y configurar un puerto fijo (la guía usa 1433).
2. Habilitar autenticación SQL Server/Mixed Mode para el usuario académico.
3. Ejecutar `database/01_crear_inventario.sql` en SSMS.
4. Verificar JDK 21 y Maven.
5. Ejecutar:

```bash
mvn clean test
mvn clean wildfly:run
```

El plugin usa `-Djboss.socket.binding.port-offset=2`, por lo que WildFly escucha en HTTP **8082** y Management **9992**. Esto permite ejecutar a la vez el `sistema-pedidos` existente (HTTP 8081, Management 9990).

Abrir:

```text
http://localhost:8082/inventario-service/
```

API principal:

```text
GET  /inventario-service/api/inventario/productos
GET  /inventario-service/api/inventario/productos/1/stock
POST /inventario-service/api/inventario/productos/1/reservas
```

Body de reserva:

```json
{
  "cantidad": 2
}
```

También existen endpoints por `codigo` (`LAP-001`, `MON-001`, `TEC-001`) para mostrar un contrato menos acoplado a las claves internas de base de datos.

## Material de clase

- `docs/GUIA_IMPLEMENTACION_PASO_A_PASO.md`
- `docs/INTEGRACION_CON_SISTEMA_PEDIDOS.md`
- `requests/inventario-service.http`
- `openapi/inventario-service.yaml`
- `database/02_reset_stock_demo.sql`

## Nota académica de seguridad

`@DataSourceDefinition` contiene usuario/contraseña para reducir pasos durante el laboratorio. En producción las credenciales no deben almacenarse en código; deben externalizarse en un DataSource administrado por WildFly, Credential Store o un mecanismo de secretos. `trustServerCertificate=true` también se usa solo para el laboratorio local.

---

## PA2 — Devolución de stock por código

### Nuevo endpoint

```text
POST /inventario-service/api/inventario/productos/codigo/{codigo}/devoluciones
Content-Type: application/json
```

Flujo: `ProductoResource` → `InventarioService` (`@Stateless`, `@TransactionAttribute(REQUIRED)`) → `ProductoInventario.devolverStock()` → SQL Server. La versión (`@Version`) se conserva, así que dos actualizaciones simultáneas del mismo producto generan `409 Conflict` en lugar de perder una de ellas.

| Caso | Respuesta |
|---|---|
| Devolución válida | `200 OK` con `codigo`, `cantidadDevuelta`, `stockRestante` |
| `cantidad` ≤ 0 o cuerpo vacío | `400 Bad Request` (el stock no cambia) |
| Código inexistente | `404 Not Found` |
| Conflicto de versión | `409 Conflict` |

Ejemplo ejecutable (PowerShell, con WildFly corriendo en 8082):

```powershell
curl.exe -s -X POST http://localhost:8082/inventario-service/api/inventario/productos/codigo/LAP-001/devoluciones -H "Content-Type: application/json" -d '{\"cantidad\":2}'
```

Respuesta (stock inicial 8):

```json
{
  "productoId": 1,
  "codigo": "LAP-001",
  "nombre": "Laptop",
  "cantidadDevuelta": 2,
  "stockRestante": 10,
  "version": 3
}
```

Más ejemplos (válido, cantidad 0 y código inexistente) en `requests/inventario-service.http`. El contrato está documentado en `openapi/inventario-service.yaml` (versión 1.4.0).

### Requisitos

- JDK 21 y Maven 3.9+.
- SQL Server con **TCP/IP habilitado en el puerto 1433** y autenticación mixta.
- Base `InventarioDB` creada con `database/01_crear_inventario.sql` (y `03_agregar_precio_catalogo.sql` si la base es anterior).

### Configuración (sin secretos)

La conexión se define en `config/database.properties` (URL `jdbc:sqlserver://localhost:1433;databaseName=InventarioDB`, usuario de laboratorio `inventario_app` y JNDI `java:jboss/datasources/InventarioDS`). La contraseña es solo de laboratorio; en un entorno real debe ir en un Credential Store de WildFly o en variables de entorno, nunca en el repositorio.

### Orden de arranque

1. **SQL Server** (servicio `MSSQLSERVER` en ejecución).
2. **WildFly**: en esta carpeta, `mvn clean wildfly:run` → `http://localhost:8082/inventario-service/`.
3. **Spring Boot** (`sistema-pedidos-springboot`), que consume esta API.

### Pruebas

```bash
mvn clean test
```

`ProductoInventarioDevolucionTest` verifica la devolución válida (8 + 2 = 10) y que una cantidad 0 o negativa se rechace sin cambiar el stock. Resultado: 5 pruebas, 0 fallos.

### Reproducir el estado inicial

`database/02_reset_stock_demo.sql` vuelve los stocks a 10 / 20 / 30.

### Limitaciones

- La devolución no es idempotente: si el cliente repite el POST se suma el stock otra vez. Por eso Spring Boot no reintenta automáticamente y controla los duplicados con la tabla `anulacion_pedido`.
- No valida que la cantidad devuelta corresponda a una reserva previa; esa regla la aplica el sistema de pedidos.
