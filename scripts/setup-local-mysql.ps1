# Install a project-local MySQL 8.0 server and initialize its data directory.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$tools = Join-Path $root '.tools'
New-Item -ItemType Directory -Force -Path $tools | Out-Null

$version = '8.0.46'
$archiveName = "mysql-$version-winx64.zip"
$archive = Join-Path $tools $archiveName
$downloadUrl = "https://cdn.mysql.com/Downloads/MySQL-8.0/$archiveName"
$checksumUrl = "$downloadUrl.md5"
$mysqlHome = Join-Path $tools "mysql-$version-winx64"
$dataDir = Join-Path $tools 'mysql-data'
$configPath = Join-Path $tools 'mysql-local.ini'

if (-not (Test-Path $archive)) {
  Write-Host "Downloading MySQL Server $version..." -ForegroundColor Cyan
  Invoke-WebRequest -UseBasicParsing -Uri $downloadUrl -OutFile $archive
}
$checksumResponse = Invoke-WebRequest -UseBasicParsing -Uri $checksumUrl
$checksumText = if ($checksumResponse.Content -is [byte[]]) {
  [System.Text.Encoding]::UTF8.GetString($checksumResponse.Content)
} else {
  [string]$checksumResponse.Content
}
$expected = [regex]::Match($checksumText, '^[a-fA-F0-9]{32}').Value.ToLowerInvariant()
$actual = (Get-FileHash -LiteralPath $archive -Algorithm MD5).Hash.ToLowerInvariant()
if (-not $expected -or $actual -ne $expected) { throw 'MySQL download checksum verification failed.' }

if (-not (Test-Path (Join-Path $mysqlHome 'bin\mysqld.exe'))) {
  Write-Host 'Verified MySQL archive; extracting...' -ForegroundColor DarkCyan
  Expand-Archive -LiteralPath $archive -DestinationPath $tools -Force
}
if (-not (Test-Path (Join-Path $mysqlHome 'bin\mysqld.exe'))) { throw 'MySQL server executable was not found after extraction.' }

$baseIni = $mysqlHome.Replace('\','/')
$dataIni = $dataDir.Replace('\','/')
$logIni = (Join-Path $tools 'mysql-error.log').Replace('\','/')
$configLines = @(
  '[mysqld]'
  "basedir=$baseIni"
  "datadir=$dataIni"
  'port=3306'
  'bind-address=127.0.0.1'
  'character-set-server=utf8mb4'
  'collation-server=utf8mb4_0900_ai_ci'
  "log-error=$logIni"
)
$configLines | Set-Content -LiteralPath $configPath -Encoding Ascii

if (-not (Test-Path (Join-Path $dataDir 'mysql'))) {
  if (Test-Path $dataDir) { throw 'MySQL data folder exists but is not initialized; inspect .tools\mysql-data before continuing.' }
  New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
  Write-Host 'Initializing local MySQL data files...' -ForegroundColor Cyan
  & (Join-Path $mysqlHome 'bin\mysqld.exe') "--defaults-file=$configPath" --initialize-insecure --console
  if ($LASTEXITCODE -ne 0) { throw 'MySQL initialization failed.' }
}

Write-Host "MySQL Server $version is installed under .tools. No Windows service or global PATH was changed." -ForegroundColor Green
Write-Host 'Start it in a separate PowerShell window with:' -ForegroundColor Green
Write-Host 'powershell -ExecutionPolicy Bypass -File scripts/start-local-mysql.ps1' -ForegroundColor Green
Write-Host 'Then set the local classroom password once with:' -ForegroundColor Green
Write-Host ('& "{0}\bin\mysql.exe" --protocol=tcp --host=127.0.0.1 --user=root --execute="ALTER USER ''root''@''localhost'' IDENTIFIED BY ''password'';"' -f $mysqlHome) -ForegroundColor Green
