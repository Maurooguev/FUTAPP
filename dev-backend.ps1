$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "futapp-back"
$stdoutLog = Join-Path $backendPath "backend-dev.log"
$stderrLog = Join-Path $backendPath "backend-dev-error.log"
$healthUrl = "http://localhost:8080/api/v1/status"
$smokeUrl = "http://localhost:8080/api/v1/torneos/pagina?pagina=0&cantidad=1"

function Test-BackendReady {
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
        if ($health.status -ne "UP" -or $health.database -ne "UP") { return $false }
        $smoke = Invoke-WebRequest -UseBasicParsing -Uri $smokeUrl -TimeoutSec 3
        return $smoke.StatusCode -eq 200
    } catch {
        return $false
    }
}

Set-Location -LiteralPath $backendPath

while ($true) {
    if (Test-BackendReady) { exit 0 }

    Add-Content -LiteralPath $stdoutLog -Value "`n[$(Get-Date -Format s)] Iniciando backend de desarrollo..."
    $backendProcess = Start-Process -FilePath (Join-Path $backendPath "mvnw.cmd") `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $backendPath `
        -RedirectStandardOutput $stdoutLog `
        -RedirectStandardError $stderrLog `
        -WindowStyle Hidden `
        -PassThru

    $startupDeadline = (Get-Date).AddSeconds(120)
    $wasHealthy = $false
    $unhealthySince = $null

    while (-not $backendProcess.HasExited) {
        $healthy = Test-BackendReady

        if ($healthy) {
            $wasHealthy = $true
            $unhealthySince = $null
        } elseif ($wasHealthy) {
            if ($null -eq $unhealthySince) { $unhealthySince = Get-Date }
            if ((Get-Date) -gt $unhealthySince.AddSeconds(25)) {
                Add-Content -LiteralPath $stderrLog -Value "`n[$(Get-Date -Format s)] El proceso sigue vivo pero la API no responde. Reiniciando el arbol de procesos."
                & taskkill.exe /PID $backendProcess.Id /T /F | Out-Null
                break
            }
        } elseif ((Get-Date) -gt $startupDeadline) {
            Add-Content -LiteralPath $stderrLog -Value "`n[$(Get-Date -Format s)] El backend no completo el inicio en 120 segundos. Reiniciando."
            & taskkill.exe /PID $backendProcess.Id /T /F | Out-Null
            break
        }

        Start-Sleep -Seconds 3
        $backendProcess.Refresh()
    }

    $exitCode = if ($backendProcess.HasExited) { $backendProcess.ExitCode } else { "forzado" }
    Add-Content -LiteralPath $stderrLog -Value "`n[$(Get-Date -Format s)] El backend se detuvo con codigo $exitCode. Se reiniciara en 3 segundos."
    Start-Sleep -Seconds 3
}
