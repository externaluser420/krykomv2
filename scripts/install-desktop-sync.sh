#!/usr/bin/env bash
# Install automatic Desktop sync so ~/Desktop/krykom2 stays updated after GitHub pushes.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SYNC_SCRIPT="$SCRIPT_DIR/sync-desktop-repo.sh"
DESKTOP_REPO="${VEIL_DESKTOP_REPO:-$HOME/Desktop/krykom2}"
BRANCH="${VEIL_SYNC_BRANCH:-cursor/premium-gui-redesign-e58c}"
INTERVAL="${VEIL_SYNC_INTERVAL:-120}"
LABEL="com.veil.desktop-sync"

log() { printf '[veil-install] %s\n' "$*"; }

if [ ! -x "$SYNC_SCRIPT" ]; then
  chmod +x "$SYNC_SCRIPT"
fi

# Initial sync so the folder exists and is up to date.
"$SYNC_SCRIPT" once

install_macos() {
  local plist="$HOME/Library/LaunchAgents/${LABEL}.plist"
  mkdir -p "$HOME/Library/LaunchAgents"

  cat >"$plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>${LABEL}</string>
  <key>ProgramArguments</key>
  <array>
    <string>/bin/bash</string>
    <string>${SYNC_SCRIPT}</string>
    <string>once</string>
  </array>
  <key>EnvironmentVariables</key>
  <dict>
    <key>VEIL_DESKTOP_REPO</key>
    <string>${DESKTOP_REPO}</string>
    <key>VEIL_SYNC_BRANCH</key>
    <string>${BRANCH}</string>
  </dict>
  <key>StartInterval</key>
  <integer>${INTERVAL}</integer>
  <key>RunAtLoad</key>
  <true/>
  <key>StandardOutPath</key>
  <string>${HOME}/Library/Logs/veil-desktop-sync.log</string>
  <key>StandardErrorPath</key>
  <string>${HOME}/Library/Logs/veil-desktop-sync.log</string>
</dict>
</plist>
EOF

  launchctl bootout "gui/$(id -u)/${LABEL}" 2>/dev/null || true
  launchctl bootstrap "gui/$(id -u)" "$plist"
  launchctl enable "gui/$(id -u)/${LABEL}"
  log "Installed macOS auto-sync (every ${INTERVAL}s)"
  log "Desktop folder: $DESKTOP_REPO"
  log "Logs: ~/Library/Logs/veil-desktop-sync.log"
}

install_cron() {
  local cron_line="${INTERVAL} seconds is not valid for cron — using */2 * * * *"
  local marker="# veil-desktop-sync"
  local cmd="VEIL_DESKTOP_REPO='${DESKTOP_REPO}' VEIL_SYNC_BRANCH='${BRANCH}' ${SYNC_SCRIPT} once >> ${HOME}/veil-desktop-sync.log 2>&1"
  local entry="*/2 * * * * ${cmd} ${marker}"

  (crontab -l 2>/dev/null | grep -v "$marker"; echo "$entry") | crontab -
  log "Installed cron job (every 2 minutes)"
  log "Desktop folder: $DESKTOP_REPO"
  log "Logs: ~/veil-desktop-sync.log"
}

case "$(uname -s)" in
  Darwin)
    install_macos
    ;;
  Linux|MINGW*|MSYS*|CYGWIN*)
    install_cron
    ;;
  *)
    log "Unknown OS — run manually: bash scripts/sync-desktop-repo.sh watch"
    ;;
esac

log "Done. Your Desktop folder will stay in sync with GitHub."
