# SkyPermissions — Wave 2 Slot #64

**Status:** engineering beta / authorization domain core.

SkyPermissions is a deterministic, fail-closed permission decision component built on the existing Java rule-engine repository. It evaluates caller-supplied subjects, roles, resources, actions, and bounded policy grants.

## Supported behavior

- normalized subject identifiers and role sets
- exact resource/action matching plus policy-side `*` wildcard grants
- deterministic priority ordering
- explicit ALLOW and DENY grants
- DENY precedence when priorities are equal
- duplicate-grant rejection
- default-deny behavior when no grant matches
- bounded counts and token validation

## Security boundary

SkyPermissions **does not authenticate users**, prove identity, establish sessions, persist policy, distribute policy, terminate TLS, enforce access at a reverse proxy, or certify a system as secure. It returns a local software decision that a consuming application must enforce correctly.

Policy administration, authenticated identity attributes, tenant isolation, policy approval/versioning, durable audit logs, emergency access, secrets, and production deployment are outside this repository's verified scope.

## SKYCOIN4444 integration contract

A consuming SKYCOIN4444 component should translate its authenticated application context into:

```text
Subject(id, roles, attributes)
Request(resource, action)
```

and evaluate those values against a reviewed set of `Grant` records. The consumer must treat `allowed=false` as a hard deny and should record the returned `reason` and `grantId` in its own audit trail.

Suggested resource naming is namespaced and deterministic, for example:

```text
wallet:account/123
community:group/42
education:course/9
enterprise:project/alpha
```

Suggested action names are narrow verbs such as `read`, `create`, `update`, `delete`, `approve`, or `admin`.

## Integration test evidence

`PermissionEngineTest` exercises default deny, explicit allow, deny precedence, priority ordering, wildcard policy behavior, input validation, and the exact `Subject -> Request -> Decision` contract that downstream SKYCOIN4444 adapters consume.

## Non-goals

This is not a complete IAM platform, identity provider, OAuth/OIDC service, ABAC language, Rego/OPA replacement, network policy enforcement plane, or compliance control. Those capabilities require separate verified components and operational evidence.
