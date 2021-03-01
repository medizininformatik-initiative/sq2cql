package com.codex.sq2cql.data_model.cql;

/*
Casts the Expression. If the cast is unsuccessful null is return.
If an exception shall be thrown use CastExpression
 */

public class TypeExpression implements Expression{
    private final Expression expression;
    private final String typeSpecifier;

    public TypeExpression(Expression expression, String typeSpecifier) {
        this.expression = expression;
        this.typeSpecifier = typeSpecifier;
    }

    @Override
    public String toString() {
        return "%s as %s".formatted(expression.toString() ,typeSpecifier);
    }
}
