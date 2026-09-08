# Remove Windows scheduled task for Desktop sync.
$TaskName = "VeilDesktopSync"

schtasks /Delete /F /TN $TaskName 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host "[veil-uninstall] Removed scheduled task '$TaskName'"
} else {
    Write-Host "[veil-uninstall] No scheduled task found"
}

Write-Host "[veil-uninstall] Auto-sync disabled. Desktop folder is unchanged."
