. "$PSScriptRoot\comun.ps1"
Titulo 'ESCENARIO 4 - Anulación con inventario-service detenido'

Paso 'Registrar el pedido 2 mientras Inventario está activo'
RegistrarPedido 'Luis Rojas' 'LAP-001' 2
Stock 'LAP-001'

Paso 'Detener inventario-service (se finaliza el proceso de WildFly que escucha en el puerto 8082)'
$conexion = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conexion) {
  Stop-Process -Id $conexion.OwningProcess -Force
  Start-Sleep -Seconds 3
  Write-Host "   WildFly detenido (PID $($conexion.OwningProcess))." -ForegroundColor Magenta
}
Http 'GET' "$Inventario/productos"

Paso 'Intentar anular el pedido 2'
Http 'POST' "$Pedidos/api/v1/anulaciones" '{"pedidoId":2,"motivo":"Solicitud del cliente"}'

Paso 'El pedido 2 conserva el estado REGISTRADO y no se creó ninguna anulación'
ListarPedidos
Http 'GET' "$Pedidos/api/v1/anulaciones"

Paso 'SQL Server sigue disponible: el stock no cambió'
Stock 'LAP-001'
