# Sky Rules — Java Rule Engine

**Status: engineering beta.** The active implementation is now actually Java 21. CI compiles with `-Xlint:all -Werror`, runs deterministic tests, builds and smoke-tests a runnable JAR, builds the container, and verifies the image runs as a non-root user. Production deployment is not verified here.

## What it does

Sky Rules evaluates declarative rules against caller-supplied facts. Rules are ordered deterministically by descending priority and then rule ID, so repeated evaluations produce stable results for the same inputs.

Supported operators:

- `EQUALS`
- `NOT_EQUALS`
- `GREATER_THAN`
- `GREATER_THAN_OR_EQUAL`
- `LESS_THAN`
- `LESS_THAN_OR_EQUAL`
- `CONTAINS`

Numeric comparisons use `BigDecimal`. The engine supports evaluating all matching rules or returning the highest-priority first match.

## Safety model

Rules are data. The engine does **not** execute JavaScript, Groovy, SpEL, shell commands, reflection, templates, or caller-provided bytecode. Rule count, fact count, identifiers, fields, expected values, and outcomes are bounded and validated.

## Build and test

```bash
mkdir -p build/main build/test
javac -Xlint:all -Werror -d build/main $(find src/main/java -name '*.java' -print)
javac -Xlint:all -Werror -cp build/main -d build/test $(find src/test/java -name '*.java' -print)
java -cp build/main:build/test com.skycoin4444.rules.RuleEngineTest
jar --create --file build/sky-rules.jar --main-class com.skycoin4444.rules.RuleCli -C build/main .
java -jar build/sky-rules.jar 150
```

Expected CLI output for `150`:

```text
example-high-value:review
```

## Container

```bash
docker build -t sky-rules .
docker run --rm sky-rules 150
docker run --rm --entrypoint=id sky-rules -u
```

The image is expected to run as UID `10001`, not root.

## Architecture

`RuleEngine` is a dependency-free Java library. A `Rule` declares a field, operator, expected value, priority, and outcome. `evaluateAll` returns every match in deterministic order; `evaluateFirst` returns the highest-priority match or `null`.

`RuleCli` is only a runnable example and container entrypoint. It is not a network service or policy-management server.

## SKYCOIN4444 integration

Keep the engine as a reusable standalone library. SKYCOIN4444 modules can wrap it behind application-specific adapters for marketplace eligibility, moderation routing, workflow decisions, education rules, or feature gating. Integrators should own rule storage, approval/versioning, authentication, authorization, audit history, and rollout controls rather than adding arbitrary code execution to this library.

## History and scope

The repository previously contained a small Python event-list service despite the Java name. That superseded code was removed from the active product branch but remains recoverable from Git history.

Sky Rules is **not** Drools, a BPM engine, a distributed policy platform, or a verified production authorization system. See [`SECURITY.md`](SECURITY.md) and [`CHANGELOG.md`](CHANGELOG.md).

## License

See `LICENSE`.
