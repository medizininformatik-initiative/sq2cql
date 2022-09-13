package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(Expression querySource, IdentifierExpression alias, boolean from) {

    public SourceClause {
        requireNonNull(querySource);
        requireNonNull(alias);
    }

    public static SourceClause of(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias, false);
    }

    public static SourceClause from(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias, true);
    }

    public String toCql(PrintContext printContext) {
        assert printContext.precedence() == 0;
        return "%s%s %s".formatted(from ? "from " : "", querySource.print(printContext.increase()), alias.print(printContext));
    }
}
