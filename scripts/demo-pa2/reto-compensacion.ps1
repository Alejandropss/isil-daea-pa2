# Requiere Spring Boot iniciado con la simulación activada (solo para esta demo):
#   mvn spring-boot:run "-Dspring-boot.run.arguments=--pedidos.simulacion.fallo-local-tras-reserva=true"
param([string]$LogSpring)

. "$PSScriptRoot\comun.ps1"
Titulo 'RETO - Compensación de la reserva ante un fallo local antes del commit de H2'

Write-Host '   Spring Boot iniciado con --pedidos.simulacion.fallo-local-tras-reserva=true (en application.properties es false)' -ForegroundColor DarkGray

Paso 'Stock inicial de LAP-001'
Stock 'LAP-001'

Paso 'Registrar un pedido de 2 unidades: Inventario reserva (10 -> 8) y luego falla la parte local'
RegistrarPedido 'Ana Torres' 'LAP-001' 2

if ($LogSpring -and (Test-Path $LogSpring)) {
  Paso 'Log de Spring Boot: reserva, fallo local y compensación'
  Get-Content $LogSpring -Encoding UTF8 |
    Select-String -Pattern 'Reserva confirmada|Fallo local tras reservar|Compensación aplicada|compensación de|incidente' |
    ForEach-Object { Write-Host ('   ' + ($_.Line -replace '^.*?\s:\s', '')) -ForegroundColor Gray }
}

Paso 'Stock final: la compensación devolvió las 2 unidades'
Stock 'LAP-001'

Paso 'El rollback de H2 dejó la tabla de pedidos sin el pedido fallido'
ListarPedidos
