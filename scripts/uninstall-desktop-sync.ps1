# Remove Windows scheduled task for Desktop sync.
$TaskName = "VeilDesktopSync"

$task = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($task) {
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "[veil-uninstall] Removed scheduled task '$TaskName'"
} else {
    Write-Host "[veil-uninstall] No scheduled task found"
}

Write-Host "[veil-uninstall] Auto-sync disabled. Desktop folder is unchanged."
