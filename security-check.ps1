$ErrorActionPreference = "Stop"

$trackedFiles = @(git ls-files)
if ($LASTEXITCODE -ne 0) { throw "No se pudo obtener la lista de archivos versionados." }

$forbiddenNames = $trackedFiles | Where-Object {
    $_ -match '(^|/)(\.env($|\.)|[^/]+\.(pem|p12|pfx|jks|keystore|key))$' -and
    $_ -notmatch '\.example$'
}
if ($forbiddenNames.Count -gt 0) {
    Write-Error ("Hay archivos sensibles versionados:`n" + ($forbiddenNames -join "`n"))
}

$secretPatterns = @(
    '-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----',
    'AKIA[0-9A-Z]{16}',
    'gh[pousr]_[A-Za-z0-9]{30,}',
    'sk_(live|prod)_[A-Za-z0-9]{16,}',
    'AIza[0-9A-Za-z_-]{35}'
)
$findings = @()
foreach ($file in $trackedFiles) {
    if ($file -match '\.(png|jpe?g|gif|webp|ico|woff2?|ttf|zip|jar)$') { continue }
    $path = Join-Path $PSScriptRoot $file
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
    $content = Get-Content -LiteralPath $path -Raw -ErrorAction SilentlyContinue
    foreach ($pattern in $secretPatterns) {
        if ($content -match $pattern) { $findings += "$file coincide con $pattern" }
    }
}
if ($findings.Count -gt 0) {
    Write-Error ("Posibles secretos encontrados:`n" + ($findings -join "`n"))
}

Write-Host "Security check OK: no hay secretos ni archivos sensibles versionados."
