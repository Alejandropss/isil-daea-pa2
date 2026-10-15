# Funciones compartidas por los scripts de demostración de la PA2.
# Requisitos: SQL Server local (autenticación de Windows), WildFly en 8082 y Spring Boot en 8081.

[Console]::OutputEncoding = [Text.Encoding]::UTF8
$OutputEncoding = [Text.Encoding]::UTF8
$ErrorActionPreference = 'Continue'

$Pedidos = 'http://localhost:8081/sistema-pedidos'
$Inventario = 'http://localhost:8082/inventario-service/api/inventario'

try {
  $Host.UI.RawUI.BufferSize = New-Object Management.Automation.Host.Size(130, 3000)
  $Host.UI.RawUI.WindowSize = New-Object Management.Automation.Host.Size(130, 52)
} catch { }

function Titulo([string]$texto) {
  $Host.UI.RawUI.WindowTitle = $texto
  Write-Host ('=' * 100) -ForegroundColor Cyan
  Write-Host "  $texto" -ForegroundColor Cyan
  Write-Host ('=' * 100) -ForegroundColor Cyan
}

function Paso([string]$texto) {
  Write-Host ''
  Write-Host ">> $texto" -ForegroundColor Yellow
}

# Consulta el stock directamente en SQL Server (fuente de verdad de Inventario).
function Stock([string]$codigo) {
  Write-Host "   [SQL Server] SELECT codigo, stock, version FROM producto_inventario WHERE codigo = '$codigo'" -ForegroundColor DarkGray
  $fila = sqlcmd -S 'lpc:localhost' -E -d InventarioDB -h -1 -W -s '|' `
    -Q "SET NOCOUNT ON; SELECT codigo, stock, version FROM dbo.producto_inventario WHERE codigo = '$codigo'"
  $partes = ("$fila").Trim().Split('|')
  Write-Host ("   {0}  ->  stock = {1}   (version {2})" -f $partes[0], $partes[1], $partes[2]) -ForegroundColor Green
}

function Color([int]$codigo) {
  if ($codigo -ge 200 -and $codigo -lt 300) { return 'Green' }
  if ($codigo -ge 500) { return 'Magenta' }
  return 'Red'
}

# Ejecuta una petición HTTP con curl.exe y muestra código y cuerpo JSON.
function Http([string]$metodo, [string]$url, [string]$json) {
  Write-Host "   $metodo $url" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $argumentos = @('-s', '-o', $salida, '-w', '%{http_code}', '--max-time', '10', '-X', $metodo, $url)
  if ($json) {
    Write-Host "   Body: $json" -ForegroundColor White
    $cuerpo = [IO.Path]::GetTempFileName()
    [IO.File]::WriteAllText($cuerpo, $json)
    $argumentos += @('-H', 'Content-Type: application/json', '--data-binary', "@$cuerpo")
  }
  $codigo = [int](& curl.exe @argumentos)
  $texto = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8)
  if ($codigo -eq 0) {
    Write-Host '   <- Sin respuesta: conexión rechazada (servicio detenido)' -ForegroundColor Magenta
    return
  }
  Write-Host "   <- HTTP $codigo" -ForegroundColor (Color $codigo)
  Mostrar $texto $codigo
}

function Mostrar([string]$texto, [int]$codigo) {
  if (-not $texto) { return }
  try { $datos = $texto | ConvertFrom-Json } catch { Write-Host "      $texto"; return }
  foreach ($o in @($datos)) {
    $props = $o.PSObject.Properties | Where-Object { $codigo -lt 400 -or $_.Name -in @('status', 'error', 'message') }
    Write-Host ('      { ' + (($props | ForEach-Object { "$($_.Name): $($_.Value)" }) -join ', ') + ' }') -ForegroundColor Gray
  }
}

# Registra un pedido usando el mismo formulario de la pantalla JSP.
function RegistrarPedido([string]$cliente, [string]$codigo, [int]$cantidad) {
  Write-Host "   POST $Pedidos/pedidos   (formulario: cliente=$cliente, productoCodigo=$codigo, cantidad=$cantidad)" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  $r = & curl.exe -s -o $salida -w '%{http_code} %{redirect_url}' -X POST "$Pedidos/pedidos" `
    --data-urlencode "cliente=$cliente" --data-urlencode "productoCodigo=$codigo" --data-urlencode "cantidad=$cantidad"
  $codigoHttp, $redireccion = "$r".Split(' ', 2)
  if ([int]$codigoHttp -eq 302) {
    Write-Host "   <- HTTP 302 Redirect -> $redireccion   (pedido registrado)" -ForegroundColor Green
  } else {
    $html = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8)
    $m = [regex]::Match($html, 'class="alerta error">\s*(.*?)\s*</div>', 'Singleline')
    Write-Host "   <- HTTP $codigoHttp   $($m.Groups[1].Value.Trim())" -ForegroundColor (Color ([int]$codigoHttp))
  }
}

function ListarPedidos {
  Write-Host "   GET $Pedidos/api/v1/orders" -ForegroundColor White
  $salida = [IO.Path]::GetTempFileName()
  & curl.exe -s --max-time 10 -o $salida "$Pedidos/api/v1/orders"
  $datos = [IO.File]::ReadAllText($salida, [Text.Encoding]::UTF8) | ConvertFrom-Json
  $lista = @($datos)
  if ($lista.Count -eq 0) { Write-Host '      (no hay pedidos en H2)' -ForegroundColor Gray; return }
  foreach ($p in $lista) {
    Write-Host ("      pedido #{0}  {1,-12} {2}  x{3}  total S/ {4}  estado = {5}" -f `
        $p.id, $p.cliente, $p.productoCodigo, $p.cantidad, $p.total, $p.estado) -ForegroundColor Gray
  }
}
