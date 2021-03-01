package com.codex.sq2cql.data_model.cql;

public class LiteralExpression implements Expression {
    private final String literal;

    public LiteralExpression(String literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}

