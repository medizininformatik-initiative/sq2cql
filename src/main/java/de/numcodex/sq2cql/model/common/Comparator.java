package de.numcodex.sq2cql.model.common;

/**
 * Comparator constants used in Structured Queries and CQL.
 */
public enum Comparator {

    EQUAL("="),
    LESS_EQUAL("<="),
    LESS_THAN("<"),
    GREATER_EQUAL(">="),
    GREATER_THAN(">");

    private final String s;

    Comparator(String s) {
        this.s = s;
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

    public String toString() {
        return s;
    }
}
