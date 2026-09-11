# Install Windows Task Scheduler job to sync Desktop/krykomv2 every 2 minutes.
param(
    [string]$DesktopRepo = "$env:USERPROFILE\Desktop\krykomv2",
    [string]$Branch = "cursor/premium-gui-redesign-e58c",
    [int]$IntervalMinutes = 2
)

$ErrorActionPreference = "Stop"
$TaskName = "VeilDesktopSync"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SyncScript = Join-Path $ScriptDir "sync-desktop-repo.ps1"

function Write-Log([string]$Message) {
    Write-Host "[veil-install] $Message"
}

if (-not (Test-Path $SyncScript)) {
    throw "Missing sync script: $SyncScript"
}

Write-Log "Running initial sync..."
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $SyncScript `
    -Mode once -DesktopRepo $DesktopRepo -Branch $Branch

$taskCommand = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$SyncScript`" -Mode once -DesktopRepo `"$DesktopRepo`" -Branch `"$Branch`""

schtasks /Delete /F /TN $TaskName 2>$null | Out-Null
schtasks /Create /F /TN $TaskName /TR $taskCommand /SC MINUTE /MO $IntervalMinutes /RL LIMITED | Out-Null

if ($LASTEXITCODE -ne 0) {
    throw "Failed to create scheduled task."
}

Write-Log "Installed scheduled task '$TaskName' (every $IntervalMinutes min)"
Write-Log "Desktop folder: $DesktopRepo"
Write-Log "Branch: $Branch"
Write-Log "Remove: schtasks /Delete /F /TN $TaskName"
Write-Log "Done."
