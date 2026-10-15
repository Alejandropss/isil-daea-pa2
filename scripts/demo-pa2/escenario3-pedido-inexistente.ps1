. "$PSScriptRoot\comun.ps1"
Titulo 'ESCENARIO 3 - Anulación de un pedido inexistente'

Paso 'Stock antes'
Stock 'LAP-001'

Paso 'Anular el pedido 999 (no existe en H2)'
Http 'POST' "$Pedidos/api/v1/anulaciones" '{"pedidoId":999,"motivo":"Prueba de pedido inexistente"}'

Paso 'Stock después: Inventario no cambió'
Stock 'LAP-001'
