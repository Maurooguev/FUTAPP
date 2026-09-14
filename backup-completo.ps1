$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backupRoot = Join-Path $projectRoot "backups"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$workDirectory = Join-Path $backupRoot "futapp-$timestamp"
$databaseFile = Join-Path $workDirectory "database.dump"
$uploadsSource = Join-Path $projectRoot "futapp-back\data\uploads"
$uploadsTarget = Join-Path $workDirectory "uploads"
$archive = "$workDirectory.zip"
$containerFile = "/tmp/futapp-$timestamp.dump"
$database = if ($env:POSTGRES_DB) { $env:POSTGRES_DB } else { "futapp" }
$username = if ($env:POSTGRES_USER) { $env:POSTGRES_USER } else { "futapp" }

New-Item -ItemType Directory -Force -Path $workDirectory | Out-Null
docker compose -f (Join-Path $projectRoot "compose.yaml") up -d --wait postgres
docker exec futapp-postgres pg_dump -U $username -d $database -Fc -f $containerFile
if ($LASTEXITCODE -ne 0) { throw "No se pudo respaldar PostgreSQL." }
docker cp "futapp-postgres:$containerFile" $databaseFile
if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el respaldo de PostgreSQL." }
docker exec futapp-postgres rm -f $containerFile | Out-Null

if (Test-Path -LiteralPath $uploadsSource) {
    Copy-Item -LiteralPath $uploadsSource -Destination $uploadsTarget -Recurse
}

Compress-Archive -LiteralPath $workDirectory -DestinationPath $archive -CompressionLevel Optimal
Remove-Item -LiteralPath $workDirectory -Recurse -Force
Write-Host "Respaldo integral creado: $archive" -ForegroundColor Green
