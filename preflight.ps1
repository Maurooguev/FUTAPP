$ErrorActionPreference = "Stop"

& "$PSScriptRoot/security-check.ps1"

Push-Location "$PSScriptRoot/futapp-front"
try {
    npm audit --omit=dev --audit-level=high
    if ($LASTEXITCODE -ne 0) { throw "npm audit detectó vulnerabilidades de producción." }
    npm run lint
    if ($LASTEXITCODE -ne 0) { throw "Falló el lint del frontend." }
    npm test
    if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas del frontend." }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "Falló la compilación del frontend." }
} finally { Pop-Location }

Push-Location "$PSScriptRoot/futapp-back"
try {
    & .\mvnw.cmd --batch-mode verify
    if ($LASTEXITCODE -ne 0) { throw "Fallaron las pruebas del backend." }
} finally { Pop-Location }

Write-Host "Preflight OK: FUTAPP está listo para desplegar."
