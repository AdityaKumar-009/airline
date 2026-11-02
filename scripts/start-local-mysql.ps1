# Run MySQL in the foreground in this PowerShell window; Ctrl+C shuts it down.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$tools = Join-Path $root '.tools'
$mysql = Join-Path $tools 'mysql-8.0.46-winx64\bin\mysqld.exe'
$config = Join-Path $tools 'mysql-local.ini'
if (-not (Test-Path $mysql) -or -not (Test-Path $config)) { throw 'Run scripts/setup-local-mysql.ps1 first.' }
if (Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet) { throw 'Port 3306 is already in use.' }
& $mysql "--defaults-file=$config" --console
