# Clean Gradle build outputs and stale caches (fixes path mismatch after folder rename).
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\clean-build.ps1
param(
    [string]$RepoRoot = ''
)

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
    $RepoRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
    if (-not (Test-Path (Join-Path $RepoRoot 'gradlew.bat'))) {
        $RepoRoot = Split-Path $PSScriptRoot -Parent
    }
}

Write-Host "[veil-clean] Repo: $RepoRoot"
Set-Location $RepoRoot

if (Test-Path '.\gradlew.bat') {
    Write-Host '[veil-clean] Stopping Gradle daemon...'
    & .\gradlew.bat --stop 2>$null
    Write-Host '[veil-clean] Running gradlew clean...'
    & .\gradlew.bat clean
}

$dirs = @(
    'build',
    'shared\build',
    'apps\android\app\build',
    '.gradle\buildOutputCleanup'
)

foreach ($dir in $dirs) {
    $full = Join-Path $RepoRoot $dir
    if (Test-Path $full) {
        Write-Host "[veil-clean] Removing $dir"
        Remove-Item -Recurse -Force $full
    }
}

Write-Host '[veil-clean] Done. Rebuild with: .\gradlew.bat :apps:android:app:assembleDebug'
