package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(RetrieveExpression retrieve, AliasExpression alias) {

    public SourceClause {
        requireNonNull(retrieve);
        requireNonNull(alias);
    }

    public static SourceClause of(RetrieveExpression retrieveExpression, AliasExpression alias) {
        return new SourceClause(retrieveExpression, alias);
    }

    public String toCql(PrintContext printContext) {
        return "from %s %s".formatted(retrieve.print(printContext), alias.print(printContext));
    }
}
