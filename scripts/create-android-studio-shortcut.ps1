# Creates an Android Studio shortcut on the Desktop (Windows 11).
# Paste into PowerShell and press Enter.

$Desktop = [Environment]::GetFolderPath("Desktop")
$ShortcutPath = Join-Path $Desktop "Android Studio.lnk"

$candidates = @(
    "$env:ProgramFiles\Android\Android Studio\bin\studio64.exe",
    "$env:LOCALAPPDATA\Programs\Android Studio\bin\studio64.exe",
    "${env:ProgramFiles(x86)}\Android\Android Studio\bin\studio64.exe"
)

$studioExe = $null
foreach ($path in $candidates) {
    if (Test-Path $path) {
        $studioExe = $path
        break
    }
}

if (-not $studioExe) {
    Write-Host "[Android Studio] Not found in usual locations." -ForegroundColor Yellow
    Write-Host "Install from: https://developer.android.com/studio"
    Write-Host "Or set path manually:"
    Write-Host '  $studioExe = "C:\path\to\Android Studio\bin\studio64.exe"'
    exit 1
}

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($ShortcutPath)
$shortcut.TargetPath = $studioExe
$shortcut.WorkingDirectory = Split-Path $studioExe
$shortcut.IconLocation = "$studioExe,0"
$shortcut.Description = "Android Studio"
$shortcut.Save()

Write-Host "[Android Studio] Shortcut created:" -ForegroundColor Green
Write-Host "  $ShortcutPath"
Write-Host "  -> $studioExe"
