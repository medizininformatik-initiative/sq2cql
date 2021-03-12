package com.codex.sq2cql.model.common;

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

    public String toString() {
        return s;
    }
}
