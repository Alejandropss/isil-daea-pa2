# PA2 — Desarrollo de Aplicaciones Empresariales Avanzado (ISIL, NRC 4622)

Anulación de pedidos con devolución de stock entre dos aplicaciones:

| Proyecto | Tecnología | Puerto | Base de datos |
|---|---|---|---|
| [`inventario-service`](inventario-service/README.md) | Jakarta EE 11 · WildFly 41 · EJB · JPA | 8082 | SQL Server (`InventarioDB`) |
| [`sistema-pedidos-springboot`](sistema-pedidos-springboot/README.md) | Spring Boot 4.1 · RestClient · Spring Data JPA | 8081 | H2 en memoria |

![Arquitectura PA2](docs/arquitectura-pa2.png)

Orden de arranque: **SQL Server → WildFly (`mvn clean wildfly:run`) → Spring Boot (`mvn spring-boot:run`)**.

Pruebas: `mvn clean test` en cada proyecto (inventario: 5 pruebas · pedidos: 11 pruebas).
