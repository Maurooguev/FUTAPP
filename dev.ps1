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

$backendProcess = Start-Process powershell `
    -WorkingDirectory $backendPath `
    -ArgumentList "-NoExit", "-Command", $backendCommand `
    -PassThru

$healthUrl = "http://localhost:8080/api/v1/status"
$backendReady = $false
Write-Host "Esperando a que el backend quede listo..."
for ($attempt = 1; $attempt -le 90; $attempt++) {
    if ($backendProcess.HasExited) {
        throw "El backend se cerro durante el inicio. Revisa la terminal del backend."
    }
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
        if ($health.status -eq "UP" -and $health.database -eq "UP") {
            $backendReady = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 1
    }
}

if (-not $backendReady) {
    throw "El backend no quedo listo en 90 segundos. Revisa la terminal del backend."
}

Write-Host "Backend y PostgreSQL listos. Iniciando frontend..."

Set-Location -LiteralPath $frontendPath
npm run dev
