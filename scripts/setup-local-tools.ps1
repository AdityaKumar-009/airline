# Download the project's pinned Windows tools into .tools without changing the system PATH.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$tools = Join-Path $root '.tools'
New-Item -ItemType Directory -Force -Path $tools | Out-Null

function Install-VerifiedZip([string]$Name, [string]$Url, [string]$ChecksumUrl, [string]$Algorithm, [string]$FilePattern) {
  $archive = Join-Path $tools $Name
  if (-not (Test-Path $archive)) {
    Write-Host "Downloading $Name..." -ForegroundColor Cyan
    Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $archive
  }

  $checksumResponse = Invoke-WebRequest -UseBasicParsing -Uri $ChecksumUrl
  $checksumText = if ($checksumResponse.Content -is [byte[]]) {
    [System.Text.Encoding]::UTF8.GetString($checksumResponse.Content)
  } else {
    [string]$checksumResponse.Content
  }
  $checksumLine = $checksumText -split "`r?`n" | Where-Object { $_ -match $FilePattern } | Select-Object -First 1
  if (-not $checksumLine) { throw "Could not find the published checksum for $Name" }
  $expected = ($checksumLine -split '\s+')[0].ToLowerInvariant()
  $actual = (Get-FileHash -LiteralPath $archive -Algorithm $Algorithm).Hash.ToLowerInvariant()
  if ($actual -ne $expected) { throw "Checksum verification failed for $Name" }

  Write-Host "Verified $Name; extracting..." -ForegroundColor DarkCyan
  Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force
}

Install-VerifiedZip `
  'node-v22.9.0-win-x64.zip' `
  'https://nodejs.org/dist/v22.9.0/node-v22.9.0-win-x64.zip' `
  'https://nodejs.org/dist/v22.9.0/SHASUMS256.txt' `
  'SHA256' `
  'node-v22\.9\.0-win-x64\.zip$'

Install-VerifiedZip `
  'OpenJDK17U-jdk_x64_windows_hotspot_17.0.12_7.zip' `
  'https://api.adoptium.net/v3/binary/version/jdk-17.0.12+7/windows/x64/jdk/hotspot/normal/eclipse' `
  'https://api.adoptium.net/v3/checksum/version/jdk-17.0.12+7/windows/x64/jdk/hotspot/normal/eclipse' `
  'SHA256' `
  '^[a-fA-F0-9]{64}'

Install-VerifiedZip `
  'apache-maven-3.9.14-bin.zip' `
  'https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip' `
  'https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip.sha512' `
  'SHA512' `
  '^4122c5e7a8794260'

$nodeHome = Join-Path $tools 'node-v22.9.0-win-x64'
$jdkHome = Get-ChildItem -LiteralPath $tools -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } | Select-Object -First 1 -ExpandProperty FullName
$mavenHome = Get-ChildItem -LiteralPath $tools -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\mvn.cmd') } | Select-Object -First 1 -ExpandProperty FullName
if (-not (Test-Path (Join-Path $nodeHome 'node.exe')) -or -not $jdkHome -or -not $mavenHome) {
  throw 'A downloaded tool was not found after extraction.'
}

$env:JAVA_HOME = $jdkHome
$env:MAVEN_HOME = $mavenHome
$env:Path = "$jdkHome\bin;$nodeHome;$mavenHome\bin;$env:Path"
& (Join-Path $jdkHome 'bin\java.exe') -version
& (Join-Path $jdkHome 'bin\javac.exe') -version
& (Join-Path $nodeHome 'node.exe') --version
& (Join-Path $nodeHome 'npm.cmd') --version
& (Join-Path $mavenHome 'bin\mvn.cmd') -version
Write-Host "`nPinned tools installed under $tools. Load them in this PowerShell session with:" -ForegroundColor Green
Write-Host '. .\scripts\use-local-tools.ps1' -ForegroundColor Green
