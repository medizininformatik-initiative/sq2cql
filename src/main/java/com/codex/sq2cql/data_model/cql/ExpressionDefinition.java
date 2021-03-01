package com.codex.sq2cql.data_model.cql;

public class ExpressionDefinition {
    private final String identifier;
    private final Expression expression;
    private AccessModifier accessModifier;

    public ExpressionDefinition(String identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    public String toString(){
        return "define %s %s : %s".formatted(accessModifier.toString(), identifier, expression.toString());
    }
}
