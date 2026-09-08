# =============================================================================
# Veil / Krykom — Windows 11 one-shot setup
# Clones (if needed) + syncs + installs auto-sync every 2 minutes.
#
# Paste this entire file into PowerShell on your PC (g g.ghost).
# =============================================================================

$DesktopRepo = "C:\Users\g g.ghost\Desktop\krykomv2"
$Branch      = "cursor/premium-gui-redesign-e58c"
$RepoUrl     = "https://github.com/externaluser420/krykomv2.git"
$TaskName    = "VeilDesktopSync"
$IntervalMin = 2

function Log([string]$msg) { Write-Host "[krykomv2] $msg" -ForegroundColor Cyan }

Log "Desktop folder: $DesktopRepo"
Log "Branch: $Branch"

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
if ($LASTEXITCODE -eq 0) {
    git checkout $Branch
} else {
    git checkout -B $Branch "origin/$Branch"
}
git pull --ff-only origin $Branch
$hash = git rev-parse --short HEAD
Log "Up to date: $Branch @ $hash"

# --- 3. Install scheduled task via schtasks (works on all Windows 11) ---
$SyncScript = Join-Path $DesktopRepo "scripts\sync-desktop-repo.ps1"

if (-not (Test-Path $SyncScript)) {
    Log "Sync script not found — using inline git pull for auto-sync."
    $taskCommand = "cmd /c cd /d `"$DesktopRepo`" && git fetch origin --prune && git pull --ff-only origin $Branch"
} else {
    $taskCommand = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$SyncScript`" -Mode once -DesktopRepo `"$DesktopRepo`" -Branch `"$Branch`""
}

schtasks /Delete /F /TN $TaskName 2>$null | Out-Null
schtasks /Create /F /TN $TaskName /TR $taskCommand /SC MINUTE /MO $IntervalMin /RL LIMITED | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Failed to create scheduled task. Try running PowerShell as Administrator."
}

Log "Auto-sync installed (every $IntervalMin min) — task: $TaskName"
Log ""
Log "DONE. Open in Android Studio:"
Log "  File -> Open -> $DesktopRepo"
Log ""
Log "Manual sync anytime:"
Log "  cd `"$DesktopRepo`""
Log "  git pull"
