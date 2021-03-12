package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

/**
 * @author Alexander Kiel
 */
public final class InvocationExpression implements Expression {

    private final Expression expression;
    private final String invocation;

    private InvocationExpression(Expression expression, String invocation) {
        this.expression = Objects.requireNonNull(expression);
        this.invocation = Objects.requireNonNull(invocation);
    }

    public static InvocationExpression of(Expression expression, String invocation) {
        return new InvocationExpression(expression, invocation);
    }

    @Override
    public String print(PrintContext printContext) {
        return "%s.%s".formatted(expression.print(printContext), invocation);
    }
}
