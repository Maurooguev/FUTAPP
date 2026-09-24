$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "futapp-back"
$stdoutLog = Join-Path $backendPath "backend-dev.log"
$stderrLog = Join-Path $backendPath "backend-dev-error.log"
$healthUrl = "http://localhost:8080/api/v1/status"

Set-Location -LiteralPath $backendPath

while ($true) {
    try {
        $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
        if ($health.status -eq "UP" -and $health.database -eq "UP") {
            exit 0
        }
    } catch {
        # El backend no esta disponible: el supervisor lo inicia abajo.
    }

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
        $healthy = $false
        try {
            $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
            $healthy = $health.status -eq "UP" -and $health.database -eq "UP"
        } catch {
            $healthy = $false
        }

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
