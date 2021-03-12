package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class ExistsExpression implements BooleanExpression {

    private final Expression expression;

    private ExistsExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    public static ExistsExpression of(Expression expression) {
        return new ExistsExpression(expression);
    }

    @Override
    public String print(PrintContext printContext) {
        return "exists(%s)".formatted(expression.print(printContext));
    }
}
