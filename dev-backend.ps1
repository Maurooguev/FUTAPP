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

    $backendProcess.WaitForExit()
    Add-Content -LiteralPath $stderrLog -Value "`n[$(Get-Date -Format s)] El backend se detuvo con codigo $($backendProcess.ExitCode). Se reiniciara en 3 segundos."
    Start-Sleep -Seconds 3
}
