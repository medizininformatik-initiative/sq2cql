package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class SourceClause {

    private final RetrieveExpression retrieve;
    private final AliasExpression alias;

    private SourceClause(RetrieveExpression retrieve, AliasExpression alias) {
        this.retrieve = Objects.requireNonNull(retrieve);
        this.alias = Objects.requireNonNull(alias);
    }

    public static SourceClause of(RetrieveExpression retrieveExpression, AliasExpression alias) {
        return new SourceClause(retrieveExpression, alias);
    }

    public String toCql(PrintContext printContext) {
        return "from %s %s".formatted(retrieve.print(printContext), alias.print(printContext));
    }
}
