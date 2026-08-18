$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "futapp-back"
$frontendPath = Join-Path $projectRoot "futapp-front"

if (-not (Test-Path -LiteralPath (Join-Path $backendPath "pom.xml"))) {
    throw "No se encontro el backend en $backendPath"
}

if (-not (Test-Path -LiteralPath (Join-Path $frontendPath "package.json"))) {
    throw "No se encontro el frontend en $frontendPath"
}

docker compose -f (Join-Path $projectRoot "compose.yaml") up -d --wait postgres
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar PostgreSQL. Verifica que Docker Desktop este abierto."
}

$backendCommand = "Set-Location -LiteralPath '$backendPath'; .\mvnw.cmd spring-boot:run"

Start-Process powershell `
    -WorkingDirectory $backendPath `
    -ArgumentList "-NoExit", "-Command", $backendCommand

Set-Location -LiteralPath $frontendPath
npm run dev
