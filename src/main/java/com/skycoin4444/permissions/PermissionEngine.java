package com.skycoin4444.permissions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic authorization decision core for SKYCOIN4444.
 *
 * <p>This class evaluates caller-supplied identity attributes and policy grants. It does not authenticate
 * identities, enforce access at a network boundary, persist policy, or provide a production IAM system.</p>
 */
public final class PermissionEngine {
    public enum Effect { ALLOW, DENY }

    public record Subject(String id, Set<String> roles, Set<String> attributes) {
        public Subject {
            id = bounded(id, "subject id", 1, 128);
            roles = normalizeSet(roles, "role", 64);
            attributes = normalizeSet(attributes, "attribute", 128);
        }
    }

    public record Request(String resource, String action) {
        public Request {
            resource = bounded(resource, "resource", 1, 160);
            action = bounded(action, "action", 1, 96);
        }
    }

    public record Grant(String id, Effect effect, String role, String resource, String action, int priority) {
        public Grant {
            id = bounded(id, "grant id", 1, 128);
            effect = Objects.requireNonNull(effect, "effect");
            role = normalizeToken(role, "role");
            resource = normalizePattern(resource, "resource");
            action = normalizePattern(action, "action");
        }
    }

    public record Decision(boolean allowed, String reason, String grantId) {}

    private final List<Grant> grants;

    public PermissionEngine(List<Grant> grants) {
        Objects.requireNonNull(grants, "grants");
        if (grants.size() > 10_000) {
            throw new IllegalArgumentException("grant count exceeds 10000");
        }
        Set<String> ids = new HashSet<>();
        for (Grant grant : grants) {
            if (!ids.add(grant.id())) {
                throw new IllegalArgumentException("duplicate grant id: " + grant.id());
            }
        }
        List<Grant> copy = new ArrayList<>(grants);
        copy.sort(Comparator.comparingInt(Grant::priority).reversed()
                .thenComparing(g -> g.effect() == Effect.DENY ? 0 : 1)
                .thenComparing(Grant::id));
        this.grants = List.copyOf(copy);
    }

    public Decision decide(Subject subject, Request request) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(request, "request");

        for (Grant grant : grants) {
            if (subject.roles().contains(grant.role())
                    && matches(grant.resource(), request.resource())
                    && matches(grant.action(), request.action())) {
                if (grant.effect() == Effect.DENY) {
                    return new Decision(false, "explicit-deny", grant.id());
                }
                return new Decision(true, "explicit-allow", grant.id());
            }
        }
        return new Decision(false, "default-deny", null);
    }

    private static boolean matches(String pattern, String actual) {
        String normalized = normalizeToken(actual, "request token");
        return pattern.equals("*") || pattern.equals(normalized);
    }

    private static Set<String> normalizeSet(Set<String> values, String label, int maxSize) {
        Objects.requireNonNull(values, label + "s");
        if (values.size() > maxSize) {
            throw new IllegalArgumentException(label + " count exceeds " + maxSize);
        }
        Set<String> result = new HashSet<>();
        for (String value : values) {
            result.add(normalizeToken(value, label));
        }
        return Set.copyOf(result);
    }

    private static String normalizePattern(String value, String label) {
        Objects.requireNonNull(value, label);
        String trimmed = value.trim();
        if (trimmed.equals("*")) {
            return "*";
        }
        return normalizeToken(trimmed, label);
    }

    private static String normalizeToken(String value, String label) {
        String normalized = bounded(value, label, 1, 160).toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9._:/-]*")) {
            throw new IllegalArgumentException(label + " contains unsupported characters");
        }
        return normalized;
    }

    private static String bounded(String value, String label, int min, int max) {
        Objects.requireNonNull(value, label);
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(label + " length must be between " + min + " and " + max);
        }
        return trimmed;
    }
}
