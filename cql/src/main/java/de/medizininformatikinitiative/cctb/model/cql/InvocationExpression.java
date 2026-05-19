package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
public record InvocationExpression(Expression<?> expression, String invocation) implements DefaultExpression {

    public InvocationExpression {
        requireNonNull(expression);
        requireNonNull(invocation);
    }

    public static InvocationExpression of(Expression<?> expression, String invocation) {
        return new InvocationExpression(expression, invocation);
    }

    @Override
    public String print(PrintContext printContext) {
        return "%s.%s".formatted(expression.print(printContext), invocation);
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new InvocationExpression(expression.withIncrementedSuffixes(suffixes()), invocation);
    }
}
