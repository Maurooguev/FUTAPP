param(
    [Parameter(Mandatory = $true)]
    [string]$BackupPath
)

$ErrorActionPreference = "Stop"
$resolvedBackup = (Resolve-Path -LiteralPath $BackupPath).Path
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$database = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { "futapp" }
$username = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "futapp" }
$containerFile = "/tmp/futapp-restore.dump"

$confirmation = Read-Host "Esto reemplazara los datos actuales de PostgreSQL. Escribi RESTAURAR para continuar"
if ($confirmation -cne "RESTAURAR") {
    Write-Host "Restauracion cancelada."
    exit 0
}

docker compose -f (Join-Path $projectRoot "compose.yaml") up -d --wait postgres
docker cp $resolvedBackup "futapp-postgres:$containerFile"
if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el respaldo al contenedor." }
docker exec futapp-postgres pg_restore -U $username -d $database --clean --if-exists --no-owner $containerFile
if ($LASTEXITCODE -ne 0) { throw "No se pudo restaurar el respaldo." }
docker exec futapp-postgres rm -f $containerFile

Write-Host "Base restaurada desde $resolvedBackup" -ForegroundColor Green
