package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(Expression retrieve, IdentifierExpression alias, boolean from) {

    public SourceClause {
        requireNonNull(retrieve);
        requireNonNull(alias);
    }

    public static SourceClause of(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias, false);
    }

    public static SourceClause from(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias, true);
    }

    public String toCql(PrintContext printContext) {
        return "%s%s %s".formatted(from ? "from " : "", retrieve.print(printContext), alias.print(printContext));
    }
}
