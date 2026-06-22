# Project Instructions

This is a fullstack project.

## General Guidelines

- **Project Goal**: The application is an iterative wedding planning tool designed to help manage wedding preparations,
  starting with guest management. Note: This goal is evolving as we work iteratively.
- **Workflow**: After any code modification, ensure you stage the affected files using `git add`.


## Backend

- All backend code is located in the `backend` directory.
- **Architecture**: Follows a DDD/Clean Architecture pattern with `api`, `application`, `domain`, and `infrastructure`
  packages. The `api` layer uses functional routing (Kotlin DSL).
  - Current API modules include guest endpoints in `backend/src/main/kotlin/me/elgregos/theweddingplan/api/guest` and auth endpoints in `backend/src/main/kotlin/me/elgregos/theweddingplan/api/auth`.
- **Technologies**: Kotlin, Spring Boot, Gradle, Exposed (for SQL), Liquibase (for DB migrations), and PostgreSQL.
- **Build & Test Commands**:
  - Run unit tests: `./gradlew test`
  - Run integration tests: `./gradlew integrationTest`
  - Run full verification (includes integration tests): `./gradlew check`
- **Testing**:
  - JUnit 5, AssertK, MockK, and Testcontainers for integration tests.
  - Use class fixtures to implement tests.
  - Use `lateinit var` for top-level test variables and initialize them in a `@BeforeTest` function.
  - Structure test methods into three distinct blocks (Given, When, Then) separated by blank lines, without explicit
    comments.
  - Use static imports for assertions, fixtures, and common utilities to improve code readability.
- **Coding Style**: For Kotlin development, adopt idiomatic Kotlin best practices: use concise syntax, favor functional
  style, and avoid verbose Java-like patterns. Specifically, use one-line functions with `=` whenever possible.
- If java is not found
  - `export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"`
  - `export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"`

## Frontend

- All frontend code is located in the `frontend` directory.
- Frontend contains two Vite apps with separate HTML entries: `frontend/public/index.html` and `frontend/backoffice/index.html`.
- **Build & Test Commands**:
  - Install dependencies: `/opt/homebrew/bin/pnpm install`
  - Run public app in development: `/opt/homebrew/bin/pnpm run dev:public`
  - Run backoffice app in development: `/opt/homebrew/bin/pnpm run dev:backoffice`
  - Run tests: `/opt/homebrew/bin/pnpm run test`
  - Vitest currently includes `backoffice/src/**/*.spec.ts` (see `frontend/vite.config.ts`).
- if pnpm or node is not found :
  - Use `pnpm` from this absolute path: `/opt/homebrew/bin/pnpm`.
  - Use `node` from this absolute path: `/Users/grego/.local/state/fnm_multishells/4700_1781734884722/bin/node`
