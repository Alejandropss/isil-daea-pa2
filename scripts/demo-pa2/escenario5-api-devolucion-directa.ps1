. "$PSScriptRoot\comun.ps1"
Titulo 'ESCENARIO 5 - Llamada directa a la nueva API de devolución de Inventario'

Paso 'Stock inicial de MON-001'
Stock 'MON-001'

Paso 'Devolución válida de 2 unidades'
Http 'POST' "$Inventario/productos/codigo/MON-001/devoluciones" '{"cantidad":2}'
Stock 'MON-001'

Paso 'Devolución inválida: cantidad 0'
Http 'POST' "$Inventario/productos/codigo/MON-001/devoluciones" '{"cantidad":0}'

Paso 'Devolución inválida: cantidad negativa'
Http 'POST' "$Inventario/productos/codigo/MON-001/devoluciones" '{"cantidad":-3}'

Paso 'Devolución inválida: código inexistente'
Http 'POST' "$Inventario/productos/codigo/NO-EXISTE/devoluciones" '{"cantidad":2}'

Paso 'Stock final: las peticiones inválidas no modificaron el stock'
Stock 'MON-001'
