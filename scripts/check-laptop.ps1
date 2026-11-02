# Run on the Windows demonstration laptop: powershell -ExecutionPolicy Bypass -File scripts/check-laptop.ps1
$ErrorActionPreference = 'Continue'
Write-Host "--- AirlineMS environment check ---" -ForegroundColor Cyan
. (Join-Path $PSScriptRoot 'use-local-tools.ps1')
$javaRelease = Get-Content (Join-Path $env:JAVA_HOME 'release') | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
$mavenInfo = (& mvn -version | Out-String)
$nodeVersion = (& node -v).Trim()
$npmVersion = (& npm -v).Trim()
Write-Host "Pinned JDK: $javaRelease"
& javac -version
Write-Host ($mavenInfo.Trim())
Write-Host "Node $nodeVersion; npm $npmVersion"
if ($javaRelease -notmatch '17\.0\.12' -or $mavenInfo -notmatch 'Maven 3\.9\.14' -or $mavenInfo -notmatch 'Java version: 17\.0\.12' -or $nodeVersion -ne 'v22.9.0' -or $npmVersion -ne '10.8.3') {
  throw 'The pinned Java/Node/npm/Maven toolchain does not match the project requirements.'
}
$frontend = Join-Path $PSScriptRoot '..\airline-frontend'
if (Test-Path (Join-Path $frontend 'node_modules\@angular\cli\package.json')) {
  Push-Location $frontend
  $cliVersion = (node -p "require('./node_modules/@angular/cli/package.json').version").Trim()
  $coreVersion = (node -p "require('./node_modules/@angular/core/package.json').version").Trim()
  Write-Host "Local Angular CLI: $cliVersion; Angular core: $coreVersion"
  Pop-Location
  if ($cliVersion -ne '19.2.27' -or $coreVersion -ne '19.2.25') { throw 'Local Angular packages do not match the pinned project versions.' }
} else { Write-Host "Run npm ci in airline-frontend to install local Angular CLI 19.2.27." -ForegroundColor Yellow }
if (Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet) {
  Write-Host 'MySQL is listening on port 3306.' -ForegroundColor Green
} else {
  Write-Host 'MySQL Server is not listening on port 3306. Workbench alone is not sufficient.' -ForegroundColor Yellow
}
