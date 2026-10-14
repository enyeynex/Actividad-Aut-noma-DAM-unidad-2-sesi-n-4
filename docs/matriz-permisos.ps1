# Matriz de permisos por rol - Actividad autonoma de la sesion 10.
# Ejecuta las mismas seis operaciones con los tres usuarios de prueba contra
# PharmaSoft (que debe estar corriendo en http://localhost:8080) e imprime el
# codigo HTTP que devolvio el servidor en cada caso.
#
# Uso (PowerShell, desde la carpeta del proyecto):
#   powershell -ExecutionPolicy Bypass -File docs\matriz-permisos.ps1

$base = "http://localhost:8080/api/v1"

function Iniciar-Sesion($usuario, $clave) {
    $cuerpo = @{ username = $usuario; password = $clave } | ConvertTo-Json
    $respuesta = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body $cuerpo
    return $respuesta.token
}

function Llamar($metodo, $ruta, $token, $cuerpo) {
    $parametros = @{
        Method          = $metodo
        Uri             = "$base$ruta"
        Headers         = @{ Authorization = "Bearer $token" }
        ContentType     = "application/json"
        UseBasicParsing = $true
    }
    if ($cuerpo) { $parametros.Body = ($cuerpo | ConvertTo-Json -Depth 5) }
    try {
        $respuesta = Invoke-WebRequest @parametros
        return [int]$respuesta.StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        return "sin respuesta"
    }
}

# Datos de apoyo: una categoria, un producto con stock y un cliente existentes.
$tokenAdmin = Iniciar-Sesion "admin" "Admin123"
$cabeceraAdmin = @{ Authorization = "Bearer $tokenAdmin" }
$categoria = (Invoke-RestMethod -Uri "$base/categorias" -Headers $cabeceraAdmin)[0].id
$producto  = ((Invoke-RestMethod -Uri "$base/productos?tamanio=100" -Headers $cabeceraAdmin).contenido | Where-Object { $_.stock -gt 0 })[0].id
$cliente   = (Invoke-RestMethod -Uri "$base/clientes?tamanio=1" -Headers $cabeceraAdmin).contenido[0].id

if (-not $categoria -or -not $producto -or -not $cliente) {
    Write-Host "Falta al menos una categoria, un producto con stock y un cliente. Registralos desde la app con admin." -ForegroundColor Yellow
    exit 1
}

$usuarios = @(
    @{ Nombre = "admin";    Clave = "Admin123" },
    @{ Nombre = "vendedor"; Clave = "Vendedor123" },
    @{ Nombre = "almacen";  Clave = "Almacen123" }
)

$resultados = @{}
foreach ($u in $usuarios) {
    $token = Iniciar-Sesion $u.Nombre $u.Clave
    $nuevoProducto = @{ nombre = "Matriz $($u.Nombre)"; precio = 1.50; stock = 5; estado = $true; categoriaId = $categoria }
    $venta = @{ clienteId = $cliente; detalles = @(@{ productoId = $producto; cantidad = 1 }) }

    $resultados[$u.Nombre] = [ordered]@{
        "Listar productos"   = Llamar "GET"  "/productos?tamanio=5" $token $null
        "Registrar producto" = Llamar "POST" "/productos" $token $nuevoProducto
        "Listar clientes"    = Llamar "GET"  "/clientes?tamanio=5" $token $null
        "Registrar venta"    = Llamar "POST" "/ventas" $token $venta
        "Ver reportes"       = Llamar "GET"  "/reportes/productos-mas-vendidos" $token $null
        "Consultar /auth/me" = Llamar "GET"  "/auth/me" $token $null
    }
}

$operaciones = $resultados["admin"].Keys
$tabla = foreach ($op in $operaciones) {
    [pscustomobject]@{
        "Operacion" = $op
        "admin"     = $resultados["admin"][$op]
        "vendedor"  = $resultados["vendedor"][$op]
        "almacen"   = $resultados["almacen"][$op]
    }
}

Write-Host ""
Write-Host "Matriz de permisos - PharmaSoft ($(Get-Date -Format 'yyyy-MM-dd HH:mm'))" -ForegroundColor Cyan
$tabla | Format-Table -AutoSize
