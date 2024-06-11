package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record AliasedQuerySource(Expression<?> querySource, IdentifierExpression alias) {

    public AliasedQuerySource {
        requireNonNull(querySource);
        requireNonNull(alias);
    }

    public static AliasedQuerySource of(Expression<?> querySource, IdentifierExpression alias) {
        return new AliasedQuerySource(querySource, alias);
    }

    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        return "%s %s".formatted(querySource.print(printContext.increase()), alias.print(printContext));
    }

    public AliasedQuerySource withIncrementedSuffixes(Map<String, Integer> increments) {
        return new AliasedQuerySource(querySource.withIncrementedSuffixes(increments),
                alias.withIncrementedSuffixes(increments));
    }
}
