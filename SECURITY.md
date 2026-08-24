# Security Policy

## Status

Sky Rules is an **engineering beta**. The active implementation is dependency-free Java 21 code compiled with `-Xlint:all -Werror`; CI runs deterministic tests, builds a runnable JAR, smoke-tests it, builds the container, and verifies a non-root runtime user. Production deployment is not verified here.

## Security boundaries

Rules are declarative data only. The engine does not execute scripts, expressions, reflection, templates, shell commands, or caller-provided bytecode. Numeric comparisons use `BigDecimal`; bounded rule/fact counts and bounded text fields reduce accidental resource abuse.

The library does not provide authentication, authorization, persistence, remote rule distribution, tenant isolation, cryptographic signing, policy governance, or audit-log durability. Integrators are responsible for those controls.

## Reporting

Use GitHub private vulnerability reporting when available. Do not put credentials, private policy data, or exploitable vulnerability details in public issues.
