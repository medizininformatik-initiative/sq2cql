package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record WithClause(AliasedQuerySource source, Expression<?> expression) implements QueryInclusionClause {

    public WithClause {
        requireNonNull(source);
        requireNonNull(expression);
    }

    public static WithClause of(AliasedQuerySource source, Expression<?> expression) {
        return new WithClause(source, expression);
    }

    @Override
    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        var increasedPrintContext = printContext.increase();
        return "with " + source.print(increasedPrintContext) + "\n" +
                increasedPrintContext.getIndent() + "such that " + expression.print(increasedPrintContext.increase());
    }

    @Override
    public WithClause withIncrementedSuffixes(Map<String, Integer> increments) {
        return new WithClause(source.withIncrementedSuffixes(increments),
                expression.withIncrementedSuffixes(increments));
    }
}
