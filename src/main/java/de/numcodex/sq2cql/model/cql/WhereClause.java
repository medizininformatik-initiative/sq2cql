package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public record WhereClause(DefaultExpression expression) implements Clause {

    public WhereClause {
        requireNonNull(expression);
    }

    public static WhereClause of(DefaultExpression expression) {
        return new WhereClause(expression);
    }

    public WhereClause map(Function<DefaultExpression, DefaultExpression> mapper) {
        return new WhereClause(mapper.apply(expression));
    }

    @Override
    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        return "where " + expression.print(printContext.increase());
    }
}
