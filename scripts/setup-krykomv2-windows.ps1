# =============================================================================
# Veil / Krykom — Windows 11 one-shot setup
# Clones (if needed) + syncs + installs auto-sync every 2 minutes.
#
# Paste this entire file into PowerShell on your PC (g.ghost).
# =============================================================================

$DesktopRepo = "C:\Users\g.ghost\Desktop\krykomv2"
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

# --- 3. Install scheduled task (auto-sync every 2 min) ---
$SyncScript = Join-Path $DesktopRepo "scripts\sync-desktop-repo.ps1"

if (-not (Test-Path $SyncScript)) {
    Log "Warning: sync script not found yet — run this again after next GitHub push."
} else {
    $action = New-ScheduledTaskAction `
        -Execute "powershell.exe" `
        -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$SyncScript`" -Mode once -DesktopRepo `"$DesktopRepo`" -Branch `"$Branch`""

    $trigger = New-ScheduledTaskTrigger `
        -Once -At (Get-Date) `
        -RepetitionInterval (New-TimeSpan -Minutes $IntervalMin) `
        -RepetitionDuration ([TimeSpan]::MaxValue)

    $settings = New-ScheduledTaskSettingsSet `
        -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries `
        -StartWhenAvailable -MultipleInstances IgnoreNew

    Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger `
        -Settings $settings -Description "Syncs Desktop\krykomv2 with GitHub" -Force | Out-Null

    Log "Auto-sync installed (every $IntervalMin min) — task: $TaskName"
}

Log ""
Log "DONE. Open in Android Studio:"
Log "  File -> Open -> $DesktopRepo"
Log ""
Log "Manual sync anytime:"
Log "  cd $DesktopRepo"
Log "  git pull"
