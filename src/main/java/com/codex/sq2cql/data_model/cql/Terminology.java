package com.codex.sq2cql.data_model.cql;

public class Terminology {
    // TerminologyExpression?
    private final Expression expression;

    public Terminology(Expression expression) {
        this.expression = expression;
    }

    public String toString()
    {
        return expression.toString();
    }
}
