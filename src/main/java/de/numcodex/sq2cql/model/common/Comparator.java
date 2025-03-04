package de.numcodex.sq2cql.model.common;

/**
 * Comparator constants used in Structured Queries and CQL.
 */
public enum Comparator {

    EQUAL("=", 6),
    EQUIVALENT("~", 6),
    LESS_EQUAL("<=", 9),
    LESS_THAN("<", 9),
    GREATER_EQUAL(">=", 9),
    GREATER_THAN(">", 9);

    private final String s;
    private final int precedence;

    Comparator(String s, int precedence) {
        this.s = s;
        this.precedence = precedence;
    }

    public static Comparator fromJson(String s) {
        return switch (s) {
            case "eq" -> EQUAL;
            case "le" -> LESS_EQUAL;
            case "lt" -> LESS_THAN;
            case "ge" -> GREATER_EQUAL;
            case "gt" -> GREATER_THAN;
            default -> throw new IllegalArgumentException("unknown JSON comparator: " + s);
        };
    }

    public int getPrecedence() {
        return precedence;
    }

    @Override
    public String toString() {
        return s;
    }
}
