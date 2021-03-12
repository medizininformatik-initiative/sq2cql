package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class TypeExpression implements Expression {

    private final Expression expression;
    private final String typeSpecifier;

    private TypeExpression(Expression expression, String typeSpecifier) {
        this.expression = Objects.requireNonNull(expression);
        this.typeSpecifier = Objects.requireNonNull(typeSpecifier);
    }

    public static TypeExpression of(Expression expression, String typeSpecifier) {
        return new TypeExpression(expression, typeSpecifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return "%s as %s".formatted(expression.print(printContext), typeSpecifier);
    }
}
