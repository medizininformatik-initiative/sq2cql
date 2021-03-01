package com.codex.sq2cql.data_model.cql;

public class SourceClause {
    private final RetrieveExpression retrieve;

    public SourceClause(RetrieveExpression retrieve) {
        this.retrieve = retrieve;
    }

    public String toString() {
        return "from %s %s".formatted(retrieve.toString(), retrieve.getNamedTypeSpecifier().getType().charAt(0));
    }

}
