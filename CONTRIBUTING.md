# Contributing to The Wedding Plan

Thanks for your interest in contributing! This project follows the workflow and conventions below,
mirrored from [AGENTS.md](AGENTS.md) (the canonical source for AI-assisted contributions).

## Project structure

- `backend/`: Kotlin + Spring Boot API (DDD/Clean Architecture: `api`, `application`, `domain`,
  `infrastructure` packages).
- `frontend/`: Vue 3 + TypeScript, two Vite apps (`frontend/public` guest-facing app,
  `frontend/backoffice` staff app).
- `docs/`: dedicated documentation (deployment, integrations, security models).

## Getting started

See [Local development setup](docs/local-development.md) to run the backend, frontend, and their
dependencies (PostgreSQL, Mailpit) locally.

## Workflow

1. Update `main` and create a new branch for your work.
2. **Branch naming**: `feat/#<issue-number><short-description>` for features,
   `fix/#<issue-number><short-description>` for bug fixes (use a similarly descriptive prefix, e.g.
   `chore/#<issue-number><short-description>`, for other changes).
3. Make your changes, following the coding conventions below.
4. Stage the affected files with `git add` after each change.
5. Do not commit or push — leave that to the maintainer.

## Versioning

For every issue (frontend, backend, or fullstack), bump and keep aligned both application versions
before staging changes:

- `frontend/package.json` (run `pnpm run version:issue`)
- `backend/build.gradle.kts` (`version = "..."`)

## Craft principles

- **KISS**: prefer the simplest implementation that satisfies the scope; avoid unnecessary
  abstraction, indirection, or overly clever code.
- **YAGNI**: implement only what is required by the current issue scope; do not add speculative
  features, extension points, or premature generalization.

## Backend conventions

- **Technologies**: Kotlin, Spring Boot, Gradle, Exposed (SQL), Liquibase (migrations),
  PostgreSQL.
- **Build & test**:
  - Unit tests: `./gradlew test`
  - Integration tests: `./gradlew integrationTest`
  - Full verification: `./gradlew check`
- **Testing conventions**:
  - JUnit 5, AssertK, MockK, Testcontainers for integration tests.
  - Reuse fixtures from the `testFixtures` source set (organized by layer: domain, API,
    infrastructure) instead of inline test data; add new fixtures there when needed, and use
    static imports for them.
  - Use `lateinit var` for top-level test variables, initialized in a `@BeforeTest` function.
  - Structure test methods into **Given / When / Then** blocks (blank lines, no comments).
  - Prefer one assertion per test method, unless multiple assertions are logically related.
  - Use static imports for assertions, fixtures, and common utilities.
  - Integration tests should extend `AbstractIntegrationTest` or
    `AbstractEndpointIntegrationTest` to inherit the reusable Testcontainers PostgreSQL setup.
- **Coding style**: idiomatic, concise, functional Kotlin — prefer one-line functions with `=`.
  The build enforces strict null-safety (`-Xjsr305=strict`).

## Frontend conventions

- **Technologies**: Vue 3, TypeScript, Vite, Vitest, Tailwind CSS, Vue Test Utils.
- **Build & test**:
  - Install dependencies: `pnpm install`
  - Run public app: `pnpm run dev:public`
  - Run backoffice app: `pnpm run dev:backoffice`
  - Run tests: `pnpm run test` (watch mode: `pnpm run test:watch`, coverage:
    `pnpm run test:coverage`)

## Pull requests

- Keep changes scoped to the linked issue.
- Reference the issue number in the PR description.
- Ensure `./gradlew check` and `pnpm run test` pass before requesting review.

