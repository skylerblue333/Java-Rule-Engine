package com.skycoin4444.rules;

import java.util.List;
import java.util.Map;

public final class RuleCli {
    private RuleCli() {}

    public static void main(String[] args) {
        RuleEngine engine = new RuleEngine(List.of(
                new RuleEngine.Rule(
                        "example-high-value",
                        100,
                        "amount",
                        RuleEngine.Operator.GREATER_THAN_OR_EQUAL,
                        "100",
                        "review")));

        String amount = args.length > 0 ? args[0] : "150";
        RuleEngine.Decision decision = engine.evaluateFirst(Map.of("amount", amount));
        if (decision == null) {
            System.out.println("no-match");
        } else {
            System.out.println(decision.ruleId() + ":" + decision.outcome());
        }
    }
}
