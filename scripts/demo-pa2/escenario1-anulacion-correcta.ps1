. "$PSScriptRoot\comun.ps1"
Titulo 'ESCENARIO 1 - Anulación correcta de un pedido de 2 unidades'

Paso 'Stock inicial de LAP-001'
Stock 'LAP-001'

Paso 'Registrar un pedido de 2 unidades (Spring Boot reserva el stock en Inventario)'
RegistrarPedido 'Ana Torres' 'LAP-001' 2
Stock 'LAP-001'
ListarPedidos

Paso 'Anular el pedido 1'
Http 'POST' "$Pedidos/api/v1/anulaciones" '{"pedidoId":1,"motivo":"Solicitud del cliente"}'

Paso 'Stock final: Inventario recibió las 2 unidades'
Stock 'LAP-001'

Paso 'El pedido NO se elimina: queda en H2 con estado ANULADO'
ListarPedidos
