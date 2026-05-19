package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record NotExpression(Expression<?> expression) implements DefaultExpression {

    public static final int PRECEDENCE = 11;

    public NotExpression {
        requireNonNull(expression);
    }

    public static <T extends Expression<T>> DefaultExpression of(T expression) {
        return new NotExpression(expression);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "not " + expression.print(printContext.withPrecedence(PRECEDENCE)));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new NotExpression(expression.withIncrementedSuffixes(increments));
    }
}
