package com.codex.sq2cql.data_model.cql;

//TODO: REMOVE THIS ONCE NO LONGER NEEDED FOR TESTING OR MOVE TO TESTING! (02.02.2021 Lorenz)
public class LiteralBoolExpression implements BooleanExpression {
    private final String literal;

    public LiteralBoolExpression(String literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
