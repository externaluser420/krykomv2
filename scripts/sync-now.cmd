@echo off
REM Auto-sync Desktop\krykomv2 with GitHub. Called by Windows Task Scheduler.
cd /d "%~dp0.."
git fetch origin --prune
git checkout cursor/premium-gui-redesign-e58c 2>nul
git pull --ff-only origin cursor/premium-gui-redesign-e58c
