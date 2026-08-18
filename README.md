[![The Wedding Plan CI](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml/badge.svg)](https://github.com/GregoryBevan/the-wedding-plan/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/GregoryBevan/the-wedding-plan/branch/main/graph/badge.svg?token=GTPKWW4VSO)](https://codecov.io/gh/GregoryBevan/the-wedding-plan)

# The Wedding Plan

An iterative wedding planning tool: guests receive a personal invitation (QR code + magic-link
sign-in) to RSVP, choose a meal, and pick a song for the wedding playlist, while the couple
manages everything from a Google-authenticated backoffice. The scope grows iteratively, starting
with guest management.

Fullstack app: Kotlin/Spring Boot backend (`backend/`) + Vue 3/TypeScript frontend (`frontend/`) —
see [CONTRIBUTING.md](CONTRIBUTING.md) for the full stack and conventions.

## Documentation

- **Getting started**
  - [Local development setup](docs/local-development.md)
  - [Deployment guide (Render + Aiven)](docs/deployment.md)
- **Guest experience**
  - [Guest area flow](docs/guest-area-flow.md)
  - [Guest magic-link authentication — security](docs/guest-magic-link-security.md)
- **Backoffice**
  - [Backoffice read-only permissions & rollout guide](docs/backoffice-read-only-permissions.md)
  - [Backoffice permissions matrix](docs/backoffice-permissions-matrix.md)
- **Integrations**
  - [Deezer integration](docs/deezer-integration.md) (song search + playlist sync)
  - [Email delivery (SMTP vs Brevo)](docs/email-delivery.md)

## Security notes

- Never commit `.env` production secrets; keep all credentials in Render secret environment
  variables.
- Guest self-service and backoffice access are two independent security models — see the
  [guest magic-link security notes](docs/guest-magic-link-security.md) and the
  [backoffice permissions guide](docs/backoffice-read-only-permissions.md).
- For IP-based protections (rate limiting), do not trust forwarded headers unless your
  ingress/proxy sanitizes them.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This project is licensed under the MIT License, subject to the Commons Clause Condition (see
[LICENSE](LICENSE)): the Software may be used, modified, and self-hosted freely, but may not be
**sold** — that includes not just reselling it as a hosted service, but any offering to third
parties, for a fee, whose value derives entirely or substantially from the Software's
functionality (e.g. paid hosting or consulting/support around it). Dywy, as licensor, is the only
party allowed to do so, including operating it as a paid, hosted commercial service (dywy.cloud).
