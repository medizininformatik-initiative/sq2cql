package de.numcodex.sq2cql.model.cql;

/**
 * Marker interface for expressions returning a boolean value.
 */
public interface BooleanExpression extends Expression {

    /**
     * An expression that always evaluates to {@code true}.
     */
    BooleanExpression TRUE = printContext -> "true";

    /**
     * An expression that always evaluates to {@code false}.
     */
    BooleanExpression FALSE = printContext -> "false";
}
