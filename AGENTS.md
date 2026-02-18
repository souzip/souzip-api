# AGENTS.md — Codex PR Review Guidelines (souzip-api)

## Goal
Review PRs for correctness, security, and maintainability with minimal noise.
Prefer actionable, specific feedback with code references.

## Review scope priority (order matters)
1) Security & auth
2) Data integrity / transactions
3) Correctness (null/edge cases, concurrency)
4) API contract & backward compatibility
5) Observability (logging, tracing, metrics)
6) Performance / N+1 / query shape
7) Tests
8) Style (only if it prevents bugs)

## Non-goals
- Do not nitpick formatting if it doesn't affect readability or bugs.
- Do not request large refactors unless clearly justified.

---

## 1) Security & Access Control
- Verify endpoints require appropriate authorization (role/permission) and no unintended anonymous access.
- Check for IDOR: user-controlled identifiers must be validated/owned/authorized.
- Validate input: request DTO constraints, whitelists for enums/codes, length limits.
- Ensure secrets are not logged and not hardcoded.
- For file upload: validate extension + MIME + size; enforce storage path safety; no path traversal.

## 2) Transactions & Consistency (Spring / JPA)
- For writes: confirm @Transactional boundaries are correct.
- Ensure failure paths roll back as intended (no swallowed exceptions).
- Avoid partial writes across multiple repositories without transaction.
- If using read-only operations: consider `@Transactional(readOnly = true)` where helpful.

## 3) Correctness & Edge Cases
- Null handling for optional fields and repository results.
- Concurrency: race conditions (e.g., "top10", counters, unique constraints).
- Timezone and date parsing/formatting correctness.
- Pagination: totals, stable ordering, deterministic results.

## 4) API Contract & Compatibility
- Response DTO fields: naming and types stable; avoid breaking changes.
- Error model: use consistent error codes/messages (match existing ErrorCode, GlobalExceptionHandler).
- HTTP semantics: status codes, idempotency, REST-ish conventions.
- Validation errors should be deterministic and helpful.

## 5) Logging & Observability
- Logs must be meaningful, avoid noisy debug logs in prod paths.
- Never log PII (email, phone, tokens, access keys).
- Include request correlation fields if project uses them.
- For audit logging (AOP): ensure action, actor, outcome, failure_reason captured correctly.

## 6) DB / Query / Performance
- Spot N+1 issues; prefer fetch joins or batch where appropriate.
- Ensure indexes likely exist for new query patterns (ORDER BY / WHERE columns).
- For "Top N" queries: ensure ordering is correct and stable.
- Avoid loading entire entities if only IDs needed (projection).

## 7) Tests
- Minimum: unit tests for core logic OR integration tests for repositories/APIs.
- If bugfix: add a regression test.
- For controller changes: add MockMvc tests if available.

## 8) Code quality rules (only when impactful)
- Avoid unused code paths.
- Prefer small, cohesive methods.
- Keep exception handling consistent; do not catch broad Exception unless rethrowing with context.
- Ensure naming matches domain terms (countryCode, souvenirId, etc.).

---

## What to comment in PR reviews
When you find an issue, provide:
- Severity: (blocker / high / medium / low)
- Why it matters (impact)
- Concrete suggestion (code-level if possible)

### Examples of good comments
- "blocker: endpoint is anonymous but exposes user data. Add @AccessMenu / auth guard..."
- "high: repository method returns List but service expects String; fix signature or mapping."

## When to approve
Approve when:
- No blocker/high issues remain
- Tests pass or are reasonably addressed
- Changes align with project conventions

## Trigger behavior
If automatic reviews are disabled, remind the author they can run:
`@codex review`