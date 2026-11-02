# Dot-source this script to use the project's portable tools in the current PowerShell session.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$tools = Join-Path $root '.tools'
$nodeHome = Join-Path $tools 'node-v22.9.0-win-x64'
$jdkHome = Get-ChildItem -LiteralPath $tools -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } | Select-Object -First 1 -ExpandProperty FullName
$mavenHome = Get-ChildItem -LiteralPath $tools -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\mvn.cmd') } | Select-Object -First 1 -ExpandProperty FullName
if (-not (Test-Path (Join-Path $nodeHome 'node.exe')) -or -not $jdkHome -or -not $mavenHome) {
  throw 'Pinned tools are missing. Run scripts/setup-local-tools.ps1 first.'
}

$env:JAVA_HOME = $jdkHome
$env:MAVEN_HOME = $mavenHome
$env:Path = "$jdkHome\bin;$nodeHome;$mavenHome\bin;$env:Path"
Write-Host "Using project-local Java, Node/npm, and Maven for this PowerShell session." -ForegroundColor Green
