package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;
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

    @Override
    public WhereClause withIncrementedSuffixes(Map<String, Integer> increments) {
        return new WhereClause(expression.withIncrementedSuffixes(increments));
    }
}
