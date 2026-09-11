#!/usr/bin/env bash
# Remove automatic Desktop sync installed by install-desktop-sync.sh
set -euo pipefail

LABEL="com.veil.desktop-sync"
MARKER="# veil-desktop-sync"

log() { printf '[veil-uninstall] %s\n' "$*"; }

case "$(uname -s)" in
  Darwin)
    launchctl bootout "gui/$(id -u)/${LABEL}" 2>/dev/null || true
    rm -f "$HOME/Library/LaunchAgents/${LABEL}.plist"
    log "Removed macOS LaunchAgent"
    ;;
  *)
    if crontab -l 2>/dev/null | grep -q "$MARKER"; then
      crontab -l 2>/dev/null | grep -v "$MARKER" | crontab -
      log "Removed cron job"
    else
      log "No cron job found"
    fi
    ;;
esac

log "Auto-sync disabled. Desktop folder is unchanged."
