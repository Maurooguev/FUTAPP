$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$seedFile = Join-Path $projectDirectory "seed-zona-oeste-demo.sql"
$demoAssets = Join-Path $projectDirectory "demo-assets"
$uploadsDirectory = Join-Path $projectDirectory "futapp-back\data\uploads"

if (-not (Test-Path -LiteralPath $seedFile)) {
    throw "No se encontró el archivo de datos demo: $seedFile"
}

New-Item -ItemType Directory -Force -Path $uploadsDirectory | Out-Null
Copy-Item -LiteralPath (Join-Path $demoAssets "escudo-demo-azul.png") -Destination (Join-Path $uploadsDirectory "escudo-demo-azul.png") -Force
Copy-Item -LiteralPath (Join-Path $demoAssets "escudo-demo-rojo.png") -Destination (Join-Path $uploadsDirectory "escudo-demo-rojo.png") -Force

$container = docker ps --filter "name=^futapp-postgres$" --format "{{.Names}}"
if ($container -ne "futapp-postgres") {
    throw "El contenedor futapp-postgres no está ejecutándose. Iniciá Docker y corré: docker start futapp-postgres"
}

docker cp $seedFile "futapp-postgres:/tmp/seed-zona-oeste-demo.sql"
docker exec futapp-postgres psql -v ON_ERROR_STOP=1 -U futapp -d futapp -f /tmp/seed-zona-oeste-demo.sql

Write-Host "Datos demo de Zona Oeste cargados correctamente." -ForegroundColor Green
