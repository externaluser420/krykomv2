# Install Windows Task Scheduler job to sync Desktop/krykom2 every 2 minutes.
param(
    [string]$DesktopRepo = "$env:USERPROFILE\Desktop\krykom2",
    [string]$Branch = "cursor/premium-gui-redesign-e58c",
    [int]$IntervalMinutes = 2
)

$ErrorActionPreference = "Stop"
$TaskName = "VeilDesktopSync"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SyncScript = Join-Path $ScriptDir "sync-desktop-repo.ps1"
$LogFile = Join-Path $env:USERPROFILE "veil-desktop-sync.log"

function Write-Log([string]$Message) {
    Write-Host "[veil-install] $Message"
}

if (-not (Test-Path $SyncScript)) {
    throw "Missing sync script: $SyncScript"
}

# Initial sync
Write-Log "Running initial sync..."
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $SyncScript `
    -Mode once -DesktopRepo $DesktopRepo -Branch $Branch

$action = New-ScheduledTaskAction `
    -Execute "powershell.exe" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$SyncScript`" -Mode once -DesktopRepo `"$DesktopRepo`" -Branch `"$Branch`""

$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Minutes $IntervalMinutes) -RepetitionDuration ([TimeSpan]::MaxValue)

$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew

Register-ScheduledTask `
    -TaskName $TaskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Description "Keeps Veil Desktop folder in sync with GitHub" `
    -Force | Out-Null

Write-Log "Installed Windows scheduled task '$TaskName' (every $IntervalMinutes min)"
Write-Log "Desktop folder: $DesktopRepo"
Write-Log "Branch: $Branch"
Write-Log "Manual sync: powershell -File scripts\sync-desktop-repo.ps1 -Mode once"
Write-Log "Remove: powershell -File scripts\uninstall-desktop-sync.ps1"
Write-Log "Done."
