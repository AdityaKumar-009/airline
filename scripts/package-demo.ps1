# Build frontend, copy static output to Gateway, and package five Spring Boot jars.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
. (Join-Path $PSScriptRoot 'use-local-tools.ps1')
Push-Location $root
try {
  if (-not (Test-Path 'keys\private.pem')) { java tools/GenerateKeys.java; if ($LASTEXITCODE -ne 0) { throw 'Key generation failed' } }
  Push-Location 'airline-frontend'
  try {
    npm ci; if ($LASTEXITCODE -ne 0) { throw 'npm ci failed' }
    npm run build; if ($LASTEXITCODE -ne 0) { throw 'Angular build failed' }
  } finally { Pop-Location }
  $source = Join-Path $root 'airline-frontend\dist\airline-frontend\browser'
  $target = Join-Path $root 'api-gateway\src\main\resources\static'
  New-Item -ItemType Directory -Force -Path $target | Out-Null
  Copy-Item (Join-Path $source '*') -Destination $target -Recurse -Force
  mvn clean package; if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
  Write-Host 'Demo jars created. Run the five Java commands from docs/IMPLEMENTATION_PLAN.md, then browse http://localhost:8080.' -ForegroundColor Green
} finally { Pop-Location }
