#!/usr/bin/env bash
# Pull the latest changes from GitHub into your local Desktop clone.
# Use this manually or via install-desktop-sync.sh (auto every 2 minutes).
set -euo pipefail

REPO_URL="${VEIL_REPO_URL:-https://github.com/externaluser420/krykomv2.git}"
DESKTOP_REPO="${VEIL_DESKTOP_REPO:-$HOME/Desktop/krykomv2}"
BRANCH="${VEIL_SYNC_BRANCH:-cursor/premium-gui-redesign-e58c}"
INTERVAL="${VEIL_SYNC_INTERVAL:-120}"

log() { printf '[veil-sync] %s\n' "$*"; }

resolve_desktop_path() {
  if [ -n "${VEIL_DESKTOP_REPO:-}" ]; then
    printf '%s' "$VEIL_DESKTOP_REPO"
    return
  fi
  case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
      printf '%s/Desktop/krykomv2' "${USERPROFILE:-$HOME}"
      ;;
    *)
      printf '%s/Desktop/krykomv2' "$HOME"
      ;;
  esac
}

sync_repo() {
  local path="$1"
  if [ ! -d "$path/.git" ]; then
    log "No git repo at $path — cloning..."
    mkdir -p "$(dirname "$path")"
    git clone "$REPO_URL" "$path"
  fi

  cd "$path"
  git fetch origin --prune

  if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
    git checkout "$BRANCH"
  elif git show-ref --verify --quiet "refs/remotes/origin/$BRANCH"; then
    git checkout -B "$BRANCH" "origin/$BRANCH"
  else
    log "Branch '$BRANCH' not found on remote. Staying on current branch."
    git pull --ff-only || true
    return
  fi

  git pull --ff-only origin "$BRANCH"
  log "Synced $path → $BRANCH ($(git rev-parse --short HEAD))"
}

watch_mode() {
  local path="$1"
  log "Watching $path every ${INTERVAL}s (Ctrl+C to stop)"
  while true; do
    sync_repo "$path" || log "Sync failed — retrying in ${INTERVAL}s"
    sleep "$INTERVAL"
  done
}

main() {
  local path
  path="$(resolve_desktop_path)"

  case "${1:-once}" in
    once)
      sync_repo "$path"
      ;;
    watch)
      watch_mode "$path"
      ;;
    path)
      printf '%s\n' "$path"
      ;;
    *)
      echo "Usage: $0 [once|watch|path]"
      exit 1
      ;;
  esac
}

main "$@"
