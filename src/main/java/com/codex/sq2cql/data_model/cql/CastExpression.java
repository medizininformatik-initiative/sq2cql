package com.codex.sq2cql.data_model.cql;

public class CastExpression implements Expression{
    private final Expression expression;
    private final String typeSpecifier;

    public CastExpression(Expression expression, String typeSpecifier) {
        this.expression = expression;
        this.typeSpecifier = typeSpecifier;
    }

    @Override
    public String toString() {
        return "cast (%s) as %s".formatted(expression.toString(), typeSpecifier);
    }
}
