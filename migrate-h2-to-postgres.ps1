$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "futapp-back"
$legacyDatabase = Join-Path $backendPath "data\futappdb.mv.db"

if (-not (Test-Path -LiteralPath $legacyDatabase)) {
    throw "No se encontro la base H2 anterior en $legacyDatabase"
}

$backendListener = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
if ($backendListener) {
    throw "El backend esta ejecutandose. Detenelo con Ctrl+C antes de migrar para liberar el archivo H2."
}

docker compose -f (Join-Path $projectRoot "compose.yaml") up -d --wait postgres
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar PostgreSQL. Verifica que Docker Desktop este abierto."
}

$legacyBasePath = $legacyDatabase.Substring(0, $legacyDatabase.Length - ".mv.db".Length).Replace("\", "/")
$previousMigrationFlag = $env:MIGRATE_H2_ENABLED
$previousLegacyUrl = $env:LEGACY_H2_URL

try {
    $env:MIGRATE_H2_ENABLED = "true"
    $env:LEGACY_H2_URL = "jdbc:h2:file:$legacyBasePath;ACCESS_MODE_DATA=r"
    Push-Location $backendPath
    .\mvnw.cmd -Ph2-migration spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "La migracion no termino correctamente. PostgreSQL se revirtio y no quedo con datos parciales."
    }
} finally {
    Pop-Location
    $env:MIGRATE_H2_ENABLED = $previousMigrationFlag
    $env:LEGACY_H2_URL = $previousLegacyUrl
}

Write-Host "Migracion completa. Ya podes iniciar Futapp normalmente con .\dev.ps1" -ForegroundColor Green
