# Sync Desktop\krykomv2 with GitHub.
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\sync-desktop-repo.ps1
param(
    [string]$DesktopRepo = '',
    [string]$Branch = 'cursor/premium-gui-redesign-e58c',
    [string]$RepoUrl = 'https://github.com/externaluser420/krykomv2.git'
)

if ([string]::IsNullOrWhiteSpace($DesktopRepo)) {
    $DesktopRepo = Join-Path $env:USERPROFILE 'Desktop\krykomv2'
}

Write-Host "[veil-sync] Target folder: $DesktopRepo"

$gitDir = Join-Path $DesktopRepo '.git'
if (-not (Test-Path $gitDir)) {
    Write-Host "[veil-sync] Cloning into $DesktopRepo ..."
    $parent = Split-Path $DesktopRepo -Parent
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    git clone $RepoUrl $DesktopRepo
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

Set-Location $DesktopRepo

git fetch origin --prune
if ($LASTEXITCODE -ne 0) { exit 1 }

git checkout $Branch
if ($LASTEXITCODE -ne 0) {
    git checkout -B $Branch ('origin/' + $Branch)
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

git pull --ff-only origin $Branch
if ($LASTEXITCODE -ne 0) { exit 1 }

$hash = git rev-parse --short HEAD
Write-Host "[veil-sync] Done. Branch $Branch at $hash"
