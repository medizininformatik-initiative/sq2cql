package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record SourceClause(AliasedQuerySource source) implements Clause {

    public SourceClause {
        requireNonNull(source);
    }

    public static SourceClause of(AliasedQuerySource source) {
        return new SourceClause(source);
    }

    @Override
    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        return "from %s".formatted(source.print(printContext.increase()));
    }
}
