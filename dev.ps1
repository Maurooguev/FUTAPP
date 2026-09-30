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

$healthUrl = "http://localhost:8080/api/v1/status"
$smokeUrl = "http://localhost:8080/api/v1/torneos/pagina?pagina=0&cantidad=1"
$backendReady = $false
$backendAlreadyRunning = $false
try {
    $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
    $smoke = Invoke-WebRequest -UseBasicParsing -Uri $smokeUrl -TimeoutSec 3
    $backendAlreadyRunning = $health.status -eq "UP" -and $health.database -eq "UP" -and $smoke.StatusCode -eq 200
} catch {
    $backendAlreadyRunning = $false
}

if (-not $backendAlreadyRunning) {
    $backendProcess = Start-Process powershell `
        -WorkingDirectory $projectRoot `
        -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $projectRoot "dev-backend.ps1") `
        -WindowStyle Hidden `
        -PassThru
}

Write-Host "Esperando a que el backend quede listo..."
for ($attempt = 1; $attempt -le 90; $attempt++) {
    if (-not $backendAlreadyRunning -and $backendProcess.HasExited) {
        throw "El supervisor del backend se cerro durante el inicio. Revisa futapp-back/backend-dev-error.log."
    }
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
        $smoke = Invoke-WebRequest -UseBasicParsing -Uri $smokeUrl -TimeoutSec 3
        if ($health.status -eq "UP" -and $health.database -eq "UP" -and $smoke.StatusCode -eq 200) {
            $backendReady = $true
            break
        }
    } catch {
        Start-Sleep -Seconds 1
    }
}

if (-not $backendReady) {
    throw "El backend no quedo listo en 90 segundos. Revisa futapp-back/backend-dev.log y backend-dev-error.log."
}

Write-Host "Backend y PostgreSQL listos. Iniciando frontend..."

Set-Location -LiteralPath $frontendPath
npm run dev
