package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

public interface Expression<T extends Expression<T>> {

    /**
     * An expression that always evaluates to {@code true}.
     */
    DefaultExpression TRUE = printContext -> "true";

    /**
     * An expression that always evaluates to {@code false}.
     */
    DefaultExpression FALSE = printContext -> "false";

    String print(PrintContext printContext);

    default Map<String, Integer> suffixes() {
        return Map.of();
    }

    T withIncrementedSuffixes(Map<String, Integer> increments);

    default boolean isIdentifier() {
        return false;
    }
}
