package com.skycoin4444.rules;

import java.util.List;
import java.util.Map;

public final class RuleEngineTest {
    private RuleEngineTest() {}

    public static void main(String[] args) {
        deterministicPriorityOrdering();
        numericComparison();
        validationRejectsUnsafeInputs();
        System.out.println("RuleEngineTest: all checks passed");
    }

    private static void deterministicPriorityOrdering() {
        RuleEngine engine = new RuleEngine(List.of(
                new RuleEngine.Rule("low", 10, "tier", RuleEngine.Operator.EQUALS, "gold", "discount-5"),
                new RuleEngine.Rule("high", 100, "tier", RuleEngine.Operator.EQUALS, "gold", "discount-15"),
                new RuleEngine.Rule("alpha", 100, "tier", RuleEngine.Operator.EQUALS, "gold", "discount-10")));

        List<RuleEngine.Decision> decisions = engine.evaluateAll(Map.of("tier", "gold"));
        require(decisions.size() == 3, "expected three matches");
        require(decisions.get(0).ruleId().equals("alpha"), "same-priority rules must sort by id");
        require(decisions.get(1).ruleId().equals("high"), "priority ordering is incorrect");
        require(engine.evaluateFirst(Map.of("tier", "gold")).ruleId().equals("alpha"), "first decision mismatch");
    }

    private static void numericComparison() {
        RuleEngine engine = new RuleEngine(List.of(
                new RuleEngine.Rule("large-order", 50, "amount", RuleEngine.Operator.GREATER_THAN_OR_EQUAL, "100", "manual-review")));

        require(engine.evaluateAll(Map.of("amount", "150.25")).size() == 1, "numeric rule should match");
        require(engine.evaluateAll(Map.of("amount", "99.99")).isEmpty(), "numeric rule should not match");
    }

    private static void validationRejectsUnsafeInputs() {
        expectFailure(() -> new RuleEngine(List.of()));
        expectFailure(() -> new RuleEngine.Rule("", 1, "field", RuleEngine.Operator.EQUALS, "x", "y"));

        RuleEngine engine = new RuleEngine(List.of(
                new RuleEngine.Rule("numeric", 1, "amount", RuleEngine.Operator.GREATER_THAN, "10", "ok")));
        expectFailure(() -> engine.evaluateAll(Map.of("amount", "not-a-number")));
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
