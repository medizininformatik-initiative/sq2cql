package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(Expression querySource, IdentifierExpression alias) {

    public SourceClause {
        requireNonNull(querySource);
        requireNonNull(alias);
    }

    public static SourceClause of(Expression querySource, IdentifierExpression alias) {
        return new SourceClause(querySource, alias);
    }

    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        return "from %s %s".formatted(querySource.print(printContext.increase()), alias.print(printContext));
    }
}
