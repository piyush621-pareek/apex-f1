# Contributing to APEX F1

This guide is how our 4-person team works on the project with Git and GitHub.
Follow it so `main` always stays working and demo-ready.

## Golden rules
1. **Never code directly on `main`.** `main` must always run for demos.
2. **Always branch from an up-to-date `main`.**
3. **One branch = one task.** Keep branches small and focused.
4. **Merge into `main` only through a Pull Request (PR).**
5. **Changes reach GitHub only after `git push`** — editing a file locally does nothing to GitHub until you commit and push.

## Branch strategy
- `main` — the stable, integrated, always-runnable app.
- `feature/<task>` — short-lived branches for each piece of work, merged into `main` via a PR, then deleted.

### Feature work (completed — weeks 7–8)
> All of these are now **done** and merged into `main` (we commit straight to main).

| Branch | Task | Owner |
|---|---|---|
| `feature/websockets` | Real-time telemetry streaming via WebSockets (done) | Akshay |
| `feature/live-dashboard` | Live Race Sim page consuming the WebSocket (done) | Sanjay Adithya |
| `feature/sim-persistence` | Save finished simulation results into MySQL (done) | Piyush |
| `feature/junit-tests` | JUnit 5 tests (done) | (team) |
| `feature/deployment` | Packaging / deployment: JAR + Docker (done) | Shaun Jaeson |

## Area ownership (who touches what)
- **Piyush** — Database & Backend (`src/main/java/...`, `database/`)
- **Akshay** — Simulation Engine (`.../simulation/`)
- **Sanjay Adithya** — Frontend (`src/main/resources/static/`)
- **Shaun Jaeson** — System Architecture & integration

## Daily workflow (Command Prompt / cmd)

**1. Get the latest main**
```cmd
cd "D:\AOOPS Project\apex-f1"
git checkout main
git pull
```

**2. Create your feature branch**
```cmd
git checkout -b feature/your-task
```

**3. Do your work, then save and upload the branch**
```cmd
git add .
git commit -m "Short message: what you changed"
git push -u origin feature/your-task
```
(Each later change on the same branch: `git add .` → `git commit -m "..."` → `git push`.)

**4. Open a Pull Request**
- Go to the repo on GitHub → click **Compare & pull request**.
- Add a title + short description → **Create pull request**.
- A teammate reviews it → **Merge pull request** → **Confirm merge**.

**5. Update local main and clean up**
```cmd
git checkout main
git pull
git branch -d feature/your-task
git push origin --delete feature/your-task
```

## Commit message tips
- Keep it short and in the present tense: `Add FSP edit endpoint`, `Fix MySQL connection flag`.
- One logical change per commit where possible.

## If a push is rejected
Someone else merged first. Sync and retry:
```cmd
git checkout main
git pull
git checkout feature/your-task
git merge main
git push
```

## Useful status commands
```cmd
git status
git branch
git log --oneline -5
git remote -v
```
