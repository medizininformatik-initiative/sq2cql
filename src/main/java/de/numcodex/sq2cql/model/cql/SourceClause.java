package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(Expression retrieve, IdentifierExpression alias) {

    public SourceClause {
        requireNonNull(retrieve);
        requireNonNull(alias);
    }

    public static SourceClause of(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias);
    }

    public String toCql(PrintContext printContext) {
        return "from %s %s".formatted(retrieve.print(printContext), alias.print(printContext));
    }
}
