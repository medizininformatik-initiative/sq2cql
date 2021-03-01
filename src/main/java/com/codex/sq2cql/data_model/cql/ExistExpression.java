package com.codex.sq2cql.data_model.cql;

public class ExistExpression implements BooleanExpression{
    private final Expression expression;

    public ExistExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "exists(%s)".formatted(expression.toString());
    }
}
