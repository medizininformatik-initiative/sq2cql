package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

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
        return "where %s".formatted(expression.print(printContext));
    }
}
