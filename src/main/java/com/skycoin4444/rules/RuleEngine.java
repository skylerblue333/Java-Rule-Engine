package com.skycoin4444.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RuleEngine {
    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        CONTAINS
    }

    public record Rule(
            String id,
            int priority,
            String field,
            Operator operator,
            String expected,
            String outcome) {
        public Rule {
            id = requireBounded(id, "id", 1, 96);
            field = requireBounded(field, "field", 1, 96);
            expected = requireBounded(expected, "expected", 0, 512);
            outcome = requireBounded(outcome, "outcome", 1, 256);
            Objects.requireNonNull(operator, "operator");
        }
    }

    public record Decision(String ruleId, String outcome) {}

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("at least one rule is required");
        }
        if (rules.size() > 10_000) {
            throw new IllegalArgumentException("rule count exceeds 10000");
        }
        this.rules = new ArrayList<>(rules);
        this.rules.sort(Comparator.comparingInt(Rule::priority).reversed().thenComparing(Rule::id));
    }

    public List<Decision> evaluateAll(Map<String, String> facts) {
        Objects.requireNonNull(facts, "facts");
        if (facts.size() > 1_000) {
            throw new IllegalArgumentException("fact count exceeds 1000");
        }

        List<Decision> decisions = new ArrayList<>();
        for (Rule rule : rules) {
            String actual = facts.get(rule.field());
            if (actual != null && matches(rule, actual)) {
                decisions.add(new Decision(rule.id(), rule.outcome()));
            }
        }
        return List.copyOf(decisions);
    }

    public Decision evaluateFirst(Map<String, String> facts) {
        List<Decision> decisions = evaluateAll(facts);
        return decisions.isEmpty() ? null : decisions.get(0);
    }

    private static boolean matches(Rule rule, String actual) {
        return switch (rule.operator()) {
            case EQUALS -> actual.equals(rule.expected());
            case NOT_EQUALS -> !actual.equals(rule.expected());
            case CONTAINS -> actual.contains(rule.expected());
            case GREATER_THAN -> number(actual).compareTo(number(rule.expected())) > 0;
            case GREATER_THAN_OR_EQUAL -> number(actual).compareTo(number(rule.expected())) >= 0;
            case LESS_THAN -> number(actual).compareTo(number(rule.expected())) < 0;
            case LESS_THAN_OR_EQUAL -> number(actual).compareTo(number(rule.expected())) <= 0;
        };
    }

    private static BigDecimal number(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("numeric operator received a non-numeric value", error);
        }
    }

    private static String requireBounded(String value, String name, int min, int max) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(name + " length must be between " + min + " and " + max);
        }
        return trimmed;
    }
}
