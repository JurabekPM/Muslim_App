# Muslim App working instructions

## Starting or resuming work

- Read `docs/PROJECT_CONTEXT.md` before changing the project. It records the user's decisions, implementation history, pending design choices, and local workflow.
- Check `git status --short`, the current branch, and recent commits. The working tree and repository are authoritative when they differ from older notes.
- Inspect the relevant code before editing. Read `README.md` for the project overview and build setup.
- Continue the user's latest request using the recorded decisions. Ask only for information that is genuinely missing.

## Collaboration and design

- Communicate with the user in Uzbek, briefly and clearly.
- Preserve existing behavior and performance. Reuse existing state, navigation, cache, theme, and press animation code.
- The user prefers soft, modern, compact designs, rounded shapes, readable short text, smooth transitions, and navy/slate with muted saffron accents in both themes.
- A request for mockups means deliver visual options first. Apply a design after the user chooses or explicitly requests implementation.
- Keep user-visible strings localized in all eight supported languages. Preserve canonical region slugs, prayer keys, and persisted identifiers.
- Test device requests on the user's physical Samsung phone when it is connected. Check the current ADB serial rather than assuming a previous address. Clearly report unavailable or unverified checks.
- Run checks appropriate to the change. Add tests for meaningful regressions when needed.

## Finishing authorized work

- The user previously requested ongoing GitHub pushes for completed project changes. Verify the intended remote, branch, staged changes, and checks before committing. Never stage unrelated user changes.
- This machine has a working copy and an Android Studio clone. Details are in `docs/PROJECT_CONTEXT.md`. Fast-forward the Studio clone only when safe, and report conflicts or local edits.
- After completing work, update `docs/PROJECT_CONTEXT.md` with the final behavior, new user decisions, remaining tasks, and actual verification results. Keep the note accurate and concise.
- Save design images that matter for future decisions under `docs/designs/` and link them from the context note. Generated files outside the repository may disappear or be unavailable on another computer.
- Include the context update with the same commit as the implementation or design deliverable when possible. Record commit history through Git rather than trying to store the hash of a commit inside itself.
- Never claim that all chat history is preserved. These files are a maintained handoff, and should capture the details needed to continue work.
