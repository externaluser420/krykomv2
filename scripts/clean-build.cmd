@echo off
cd /d "%~dp0.."
echo [veil-clean] Repo: %CD%
if exist gradlew.bat (
    call gradlew.bat --stop 2>nul
    call gradlew.bat clean
)
if exist build rmdir /s /q build
if exist shared\build rmdir /s /q shared\build
if exist apps\android\app\build rmdir /s /q apps\android\app\build
echo [veil-clean] Done. Rebuild with: gradlew.bat :apps:android:app:assembleDebug
