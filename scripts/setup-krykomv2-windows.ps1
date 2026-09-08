# =============================================================================
# Veil / Krykom — Windows 11 one-shot setup
# Paste this entire block into PowerShell (Run as Administrator).
# =============================================================================

$DesktopRepo = "C:\Users\g g.ghost\Desktop\krykomv2"
$Branch      = "cursor/premium-gui-redesign-e58c"
$RepoUrl     = "https://github.com/externaluser420/krykomv2.git"
$TaskName    = "VeilDesktopSync"
$IntervalMin = 2

function Log([string]$msg) { Write-Host "[krykomv2] $msg" -ForegroundColor Cyan }

Log "Desktop folder: $DesktopRepo"

# --- 1. Clone if missing ---
if (-not (Test-Path "$DesktopRepo\.git")) {
    Log "Cloning repo..."
    New-Item -ItemType Directory -Force -Path (Split-Path $DesktopRepo) | Out-Null
    git clone $RepoUrl $DesktopRepo
    if ($LASTEXITCODE -ne 0) { throw "git clone failed — install Git from https://git-scm.com/download/win" }
}

Set-Location $DesktopRepo

# --- 2. Sync now ---
Log "Syncing latest code..."
git fetch origin --prune
git show-ref --verify --quiet "refs/heads/$Branch" 2>$null
if ($LASTEXITCODE -eq 0) { git checkout $Branch } else { git checkout -B $Branch "origin/$Branch" }
git pull --ff-only origin $Branch
Log "Up to date: $Branch @ $(git rev-parse --short HEAD)"

# --- 3. Scheduled task via .cmd wrapper (handles spaces in path) ---
$SyncCmd = Join-Path $DesktopRepo "scripts\sync-now.cmd"

if (-not (Test-Path (Split-Path $SyncCmd))) {
    New-Item -ItemType Directory -Force -Path (Split-Path $SyncCmd) | Out-Null
}

if (-not (Test-Path $SyncCmd)) {
    @"
@echo off
cd /d "%~dp0.."
git fetch origin --prune
git checkout $Branch 2>nul
git pull --ff-only origin $Branch
"@ | Set-Content -Path $SyncCmd -Encoding ASCII
    Log "Created $SyncCmd"
}

schtasks /Delete /F /TN $TaskName 2>$null | Out-Null

# schtasks needs nested quotes when path contains spaces
$trArg = "`"$SyncCmd`""
$result = schtasks /Create /F /TN $TaskName /TR $trArg /SC MINUTE /MO $IntervalMin 2>&1

if ($LASTEXITCODE -ne 0) {
    Log "schtasks output: $result"
    throw "Scheduled task failed. See output above."
}

Log "Auto-sync installed (every $IntervalMin min) — task: $TaskName"
Log ""
Log "DONE. Open in Android Studio:"
Log "  File -> Open -> $DesktopRepo"
