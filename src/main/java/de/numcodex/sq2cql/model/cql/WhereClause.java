package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class WhereClause {

    private final Expression expression;

    private WhereClause(Expression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    public static WhereClause of(Expression expression) {
        return new WhereClause(expression);
    }

    public String toCql(PrintContext printContext) {
        return "where " + expression.print(printContext.resetPrecedence().increase());
    }
}
