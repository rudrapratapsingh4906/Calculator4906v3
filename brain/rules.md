# Project Rules & Constraints

## Mandatory Development Rules
1.  **Read-Only Default**: Always read files before making any assumptions.
2.  **Strict Preservation**: 100% preservation of existing code, UI, features, architecture, and business logic.
3.  **No Unapproved Changes**: No modifications allowed until the user explicitly writes **"APPROVED"**.
4.  **Minimalist Fixing**: Only fix specific identified bugs with the minimum number of line changes.
5.  **Rename Restriction**: Do NOT rename files, packages, variables, or functions.
6.  **Structural Lock**: Do NOT refactor or change the multi-module architecture.
7.  **Gradle Safety**: Preserve release signing configurations while ensuring they don't break the build environment.
8.  **Documentation Discipline**: Never document temporary workspace changes as permanent project changes. Always distinguish between environment-only fixes and source repository modifications.
9.  **Approval Required**: Never modify any source code without explicit APPROVED.
10. **Scope Control**: Never edit unrelated files.
11. **Root Cause Analysis**: Always identify the root cause before suggesting changes.
12. **Stability First**: Create a stable checkpoint before any modification.
13. **Verification Update**: After every successful fix, update checkpoint.md and changelog.md only if the fix has been verified in the actual project repository.
14. **Read First**: Always read the relevant files completely before proposing or making any changes.
15. **Minimal Changes**: When approved, modify only the minimum required lines. Do not reformat, rewrite, or change unrelated code. Preserve comments, formatting, architecture, and existing functionality.

## AI Development Efficiency Rules

### 1. Token Saving Debug Workflow
- Do not scan the full project for small issues.
- Analyze only the reported problem.
- Read only relevant files required for that issue.
- Do not explore unrelated modules.
- Fix one issue at a time.
- Avoid unnecessary improvements or refactoring.
- Stop after analysis and wait for APPROVED.

### 2. Minimal Change Workflow
- Before any fix, identify:
  - Root cause
  - Exact affected files
  - Why those files need changes
- After approval, modify only the minimum required lines.
- Preserve existing architecture, UI, features, and functionality.
- Verify only the approved change.
- Stop immediately after completion.

### 3. Stability Priority
- Prefer bug fixing and verification over new features.
- Do not rebuild unless files are changed or verification requires it.
- Keep Workspace changes separate from Local project and GitHub changes.

## Communication Protocol
- Provide analysis first.
- Identify exact files and line numbers before editing.
- Explain "Why" before "What".
- Wait for approval.
