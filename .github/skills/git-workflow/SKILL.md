---
name: "manage-git-workflow"
description: "Execute Git workflows: branching, rebasing, cherry-picking, and PR preparation. Use when: creating feature branches, handling PR merges, rebasing against develop, or managing complex branch operations."
user-invocable: true
---

# Git Workflow Skill

Execute safe and efficient Git workflows: branching, rebasing, merging, and PR preparation.

## When to Use This Skill

- **Creating a feature branch**: You're starting new work
- **Rebasing before merge**: You want a clean commit history
- **Handling merge conflicts**: You need to resolve conflicts carefully
- **Cherry-picking commits**: You want specific commits from another branch
- **Cleaning up branches**: You want to delete or reorganize branches
- **Preparing a PR**: You want to ensure your branch is ready for review

## What This Skill Provides

### Git Workflow Templates
Safe, step-by-step procedures for common Git operations.

Location: `.github/skills/git-workflow/assets/`

### Branch Naming Convention
Consistent branch naming for team coordination.

### Merge Strategy
How to merge PRs, handle conflicts, and maintain clean history.

## Workflow

### 1. Create a Feature Branch

**Branch from**: `develop`

**Branch name**: Follow the pattern in [development.instructions.md](../../instructions/development.instructions.md)

```bash
# Fetch latest from remote
git fetch origin

# Create and check out a new branch
git checkout -b feature/CARGO-123-order-tracking develop

# Push to remote
git push -u origin feature/CARGO-123-order-tracking
```

**Verify**:
```bash
# Confirm you're on the right branch
git status
# On branch feature/CARGO-123-order-tracking

# Confirm develop is up-to-date
git log -1 --format="%H %s"
```

### 2. Make Commits

Follow the commit message format in [development.instructions.md](../../instructions/development.instructions.md).

```bash
# Stage your changes
git add src/

# Commit with a clear message
git commit -m "feat(order): add shipment tracking API

Implement /orders/{id}/shipments endpoint to retrieve all shipments
for an order with real-time tracking data."

# Push to remote
git push origin feature/CARGO-123-order-tracking
```

**Guidelines**:
- ✅ One logical change per commit
- ✅ Clear, descriptive commit messages
- ✅ Tests pass locally before committing
- ❌ Don't commit debug code or secrets
- ❌ Avoid "work in progress" commits

### 3. Prepare for Merge

Before opening a PR or merging, ensure your branch is clean and up-to-date.

#### Option A: Rebase (Preferred for Clean History)

```bash
# Fetch latest
git fetch origin

# Rebase your branch onto develop
git rebase origin/develop

# If conflicts, resolve them
# Then continue rebase
git rebase --continue

# Force-push to remote (because history changed)
git push -f origin feature/CARGO-123-order-tracking
```

**Use rebase when**:
- Your branch has multiple commits
- You want a clean, linear history
- The PR will be squashed anyway

#### Option B: Merge (For Keeping All History)

```bash
# Fetch latest
git fetch origin

# Merge develop into your branch
git merge origin/develop

# If conflicts, resolve them
git add .
git commit -m "merge: resolve conflicts with develop"

# Push to remote
git push origin feature/CARGO-123-order-tracking
```

**Use merge when**:
- You want to preserve all commit history
- Multiple people are working on the branch
- The branch represents a significant feature

### 4. Open a Pull Request

Use the PR template and follow the process in [code-review.instructions.md](../../instructions/code-review.instructions.md).

### 5. Merge After Approval

**Option A: Squash and Merge** (Recommended)
Combine all commits into one for a clean history.

```bash
# From GitHub UI: Select "Squash and merge"
# Or from CLI:
git checkout develop
git fetch origin
git merge --squash feature/CARGO-123-order-tracking
git commit -m "feat(order): add shipment tracking API (#PR-NUMBER)"
git push origin develop
```

**Option B: Rebase and Merge**
Reapply your commits on top of develop.

```bash
git checkout develop
git fetch origin
git rebase origin/feature/CARGO-123-order-tracking
git push origin develop
```

**Option C: Regular Merge** (For complex features)
Preserve full history with a merge commit.

```bash
git checkout develop
git fetch origin
git merge --no-ff feature/CARGO-123-order-tracking -m "Merge branch 'feature/CARGO-123-order-tracking' into develop"
git push origin develop
```

### 6. Cleanup

```bash
# Delete local branch
git branch -d feature/CARGO-123-order-tracking

# Delete remote branch
git push origin --delete feature/CARGO-123-order-tracking
```

## Common Scenarios

### Handling Merge Conflicts

```bash
# During rebase or merge, you get a conflict
git status  # See which files have conflicts

# Edit the conflicted files
# Look for markers: <<<<<<<, =======, >>>>>>>
# Keep the version you want (or both)

# Stage the resolved files
git add .

# Continue the rebase
git rebase --continue

# Or finalize the merge
git commit -m "merge: resolve conflicts"
```

### Cherry-Picking a Specific Commit

```bash
# Find the commit hash you want
git log develop --oneline | head -20

# Cherry-pick it onto your branch
git cherry-pick <commit-hash>

# If conflicts, resolve and continue
git cherry-pick --continue
```

### Cleaning Up Old Branches

```bash
# List branches (local)
git branch

# List branches (remote)
git branch -r

# Delete local branch
git branch -d feature/old-branch

# Delete remote branch
git push origin --delete feature/old-branch

# Prune local references to deleted remote branches
git fetch origin --prune
```

### Syncing Your Branch with Develop

```bash
# Fetch latest
git fetch origin

# See what changed in develop
git log --oneline origin/develop -10

# Rebase your branch onto the latest develop
git rebase origin/develop

# If you get conflicts, resolve and continue
git rebase --continue

# Force-push to remote
git push -f origin feature/your-branch-name
```

## Best Practices

✅ **Do**:
- Pull latest `develop` before creating a feature branch
- Rebase before opening a PR
- Keep commits small and logical
- Write clear commit messages
- Resolve conflicts manually, don't just accept one side
- Delete branches after merging

❌ **Don't**:
- Commit debug code or secrets
- Force-push to `main` or `develop`
- Merge without approval
- Ignore conflicts or use auto-merge
- Work directly on `develop` or `main`
- Leave stale branches in the repository

## Troubleshooting

### "Your branch has diverged"
You pushed commits, then someone else pushed. Rebase or merge to sync.

```bash
git rebase origin/develop
# or
git merge origin/develop
```

### "Detached HEAD"
You checked out a specific commit instead of a branch. Return to your branch:

```bash
git checkout feature/your-branch-name
```

### "Oh no, I force-pushed and lost commits"
Git stores recent commits in the reflog. Recover them:

```bash
git reflog  # Find the lost commit hash
git checkout <hash>  # Go back to that state
git branch feature/recovered-work  # Create a new branch
```

### "I committed to the wrong branch"
Create a new branch with your commits, then reset the old branch:

```bash
git branch feature/correct-branch    # Create new branch
git checkout develop                 # Go back to develop
git reset --hard HEAD~N              # Undo last N commits
git checkout feature/correct-branch  # Return to new branch
```

## Related Standards

- **Branch naming**: See [development.instructions.md](../../instructions/development.instructions.md)
- **Commit messages**: See [development.instructions.md](../../instructions/development.instructions.md)
- **Code review**: See [code-review.instructions.md](../../instructions/code-review.instructions.md)
- **Project standards**: See [copilot-instructions.md](../../copilot-instructions.md)

---

Questions? Ask `@Developer` for help with Git workflows, or check the troubleshooting section above.
