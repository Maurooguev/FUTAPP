$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backupDirectory = Join-Path $projectRoot "backups"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$fileName = "futapp-$timestamp.dump"
$target = Join-Path $backupDirectory $fileName
$containerFile = "/tmp/$fileName"
$database = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { "futapp" }
$username = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "futapp" }

New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null
docker compose -f (Join-Path $projectRoot "compose.yaml") up -d --wait postgres
docker exec futapp-postgres pg_dump -U $username -d $database -Fc -f $containerFile
if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el respaldo dentro de PostgreSQL." }
docker cp "futapp-postgres:$containerFile" $target
if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el respaldo a $target" }
docker exec futapp-postgres rm -f $containerFile

Write-Host "Respaldo creado: $target" -ForegroundColor Green
