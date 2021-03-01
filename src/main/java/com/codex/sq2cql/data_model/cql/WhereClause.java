package com.codex.sq2cql.data_model.cql;

public class WhereClause{
    private final Expression expression;

    public WhereClause(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "where %s".formatted(expression.toString());
    }
}
