. "$PSScriptRoot\comun.ps1"
Titulo 'ESCENARIO 2 - Anulación duplicada del mismo pedido'

Paso 'Stock antes del segundo intento'
Stock 'LAP-001'

Paso 'Intentar anular otra vez el pedido 1'
Http 'POST' "$Pedidos/api/v1/anulaciones" '{"pedidoId":1,"motivo":"Solicitud del cliente"}'

Paso 'Stock después: NO se devolvió stock por segunda vez'
Stock 'LAP-001'

Paso 'Sigue existiendo un único registro de anulación'
Http 'GET' "$Pedidos/api/v1/anulaciones"
