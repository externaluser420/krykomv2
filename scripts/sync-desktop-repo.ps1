# Pull latest GitHub changes into your Desktop clone (Windows 11).
param(
    [ValidateSet("once", "watch", "path")]
    [string]$Mode = "once",
    [string]$DesktopRepo = "$env:USERPROFILE\Desktop\krykom2",
    [string]$Branch = "cursor/premium-gui-redesign-e58c",
    [string]$RepoUrl = "https://github.com/externaluser420/krykomv2.git",
    [int]$IntervalSeconds = 120
)

function Write-Log([string]$Message) {
    Write-Host "[veil-sync] $Message"
}

function Sync-Repo {
    param([string]$Path)

    if (-not (Test-Path "$Path\.git")) {
        Write-Log "No git repo at $Path — cloning..."
        New-Item -ItemType Directory -Force -Path (Split-Path $Path) | Out-Null
        git clone $RepoUrl $Path
        if ($LASTEXITCODE -ne 0) { throw "git clone failed" }
    }

    Push-Location $Path
    try {
        git fetch origin --prune
        if ($LASTEXITCODE -ne 0) { throw "git fetch failed" }

        $localBranch = git show-ref --verify --quiet "refs/heads/$Branch"; $hasLocal = ($LASTEXITCODE -eq 0)
        git show-ref --verify --quiet "refs/remotes/origin/$Branch" | Out-Null
        $hasRemote = ($LASTEXITCODE -eq 0)

        if ($hasLocal) {
            git checkout $Branch
        } elseif ($hasRemote) {
            git checkout -B $Branch "origin/$Branch"
        } else {
            Write-Log "Branch '$Branch' not found on remote. Staying on current branch."
            git pull --ff-only 2>$null
            return
        }

        git pull --ff-only origin $Branch
        if ($LASTEXITCODE -ne 0) { throw "git pull failed" }

        $hash = git rev-parse --short HEAD
        Write-Log "Synced $Path -> $Branch ($hash)"
    } finally {
        Pop-Location
    }
}

switch ($Mode) {
    "once" {
        Sync-Repo -Path $DesktopRepo
    }
    "watch" {
        Write-Log "Watching $DesktopRepo every ${IntervalSeconds}s (Ctrl+C to stop)"
        while ($true) {
            try {
                Sync-Repo -Path $DesktopRepo
            } catch {
                Write-Log "Sync failed: $_"
            }
            Start-Sleep -Seconds $IntervalSeconds
        }
    }
    "path" {
        Write-Output $DesktopRepo
    }
}
