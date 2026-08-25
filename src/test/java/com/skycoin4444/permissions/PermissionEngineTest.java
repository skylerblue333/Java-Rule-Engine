package com.skycoin4444.permissions;

import java.util.List;
import java.util.Set;

public final class PermissionEngineTest {
    private PermissionEngineTest() {}

    public static void main(String[] args) {
        defaultDeny();
        explicitAllow();
        denyWinsAtSamePriority();
        higherPriorityRuleWins();
        wildcardIsPolicySideOnly();
        validationRejectsUnsafeInputs();
        System.out.println("PermissionEngineTest: all checks passed");
    }

    private static void defaultDeny() {
        PermissionEngine engine = new PermissionEngine(List.of());
        PermissionEngine.Decision decision = engine.decide(
                new PermissionEngine.Subject("user-1", Set.of("member"), Set.of()),
                new PermissionEngine.Request("project:alpha", "read"));
        require(!decision.allowed(), "unknown access must fail closed");
        require(decision.reason().equals("default-deny"), "default deny reason mismatch");
    }

    private static void explicitAllow() {
        PermissionEngine engine = new PermissionEngine(List.of(
                new PermissionEngine.Grant("reader", PermissionEngine.Effect.ALLOW, "member", "project:alpha", "read", 10)));
        PermissionEngine.Decision decision = engine.decide(
                new PermissionEngine.Subject("user-1", Set.of("MEMBER"), Set.of("department:ops")),
                new PermissionEngine.Request("PROJECT:ALPHA", "READ"));
        require(decision.allowed(), "matching allow grant should permit access");
        require(decision.grantId().equals("reader"), "grant id mismatch");
    }

    private static void denyWinsAtSamePriority() {
        PermissionEngine engine = new PermissionEngine(List.of(
                new PermissionEngine.Grant("allow", PermissionEngine.Effect.ALLOW, "member", "project:alpha", "read", 20),
                new PermissionEngine.Grant("deny", PermissionEngine.Effect.DENY, "member", "project:alpha", "read", 20)));
        PermissionEngine.Decision decision = engine.decide(
                new PermissionEngine.Subject("u", Set.of("member"), Set.of()),
                new PermissionEngine.Request("project:alpha", "read"));
        require(!decision.allowed(), "deny must win at equal priority");
        require(decision.grantId().equals("deny"), "deny grant should be reported");
    }

    private static void higherPriorityRuleWins() {
        PermissionEngine engine = new PermissionEngine(List.of(
                new PermissionEngine.Grant("low-deny", PermissionEngine.Effect.DENY, "admin", "settings", "write", 1),
                new PermissionEngine.Grant("high-allow", PermissionEngine.Effect.ALLOW, "admin", "settings", "write", 100)));
        PermissionEngine.Decision decision = engine.decide(
                new PermissionEngine.Subject("u", Set.of("admin"), Set.of()),
                new PermissionEngine.Request("settings", "write"));
        require(decision.allowed(), "higher priority grant should be evaluated first");
        require(decision.grantId().equals("high-allow"), "priority order mismatch");
    }

    private static void wildcardIsPolicySideOnly() {
        PermissionEngine engine = new PermissionEngine(List.of(
                new PermissionEngine.Grant("ops-read", PermissionEngine.Effect.ALLOW, "ops", "*", "read", 10)));
        require(engine.decide(
                new PermissionEngine.Subject("u", Set.of("ops"), Set.of()),
                new PermissionEngine.Request("service:billing", "read")).allowed(), "policy wildcard should match");
        expectFailure(() -> new PermissionEngine.Request("*", "read"));
    }

    private static void validationRejectsUnsafeInputs() {
        expectFailure(() -> new PermissionEngine.Grant("g", PermissionEngine.Effect.ALLOW, "admin;drop", "x", "read", 1));
        expectFailure(() -> new PermissionEngine(List.of(
                new PermissionEngine.Grant("dup", PermissionEngine.Effect.ALLOW, "a", "x", "read", 1),
                new PermissionEngine.Grant("dup", PermissionEngine.Effect.DENY, "b", "x", "read", 2))));
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected failure");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
