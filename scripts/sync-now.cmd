@echo off
REM Sync Desktop\krykomv2 with GitHub.
set "REPO=%USERPROFILE%\Desktop\krykomv2"
set "BRANCH=cursor/premium-gui-redesign-e58c"

if not exist "%REPO%\.git" (
    echo [veil-sync] Cloning into %REPO%...
    git clone https://github.com/externaluser420/krykomv2.git "%REPO%"
    if errorlevel 1 exit /b 1
)

cd /d "%REPO%"
git fetch origin --prune
if errorlevel 1 exit /b 1

git checkout %BRANCH%
if errorlevel 1 (
    git checkout -B %BRANCH% origin/%BRANCH%
    if errorlevel 1 exit /b 1
)

git pull --ff-only origin %BRANCH%
if errorlevel 1 exit /b 1

for /f %%h in ('git rev-parse --short HEAD') do set HASH=%%h
echo [veil-sync] Synced %REPO% -^> %BRANCH% (%HASH%)
