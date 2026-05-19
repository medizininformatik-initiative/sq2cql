package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record WrapperExpression(Expression<?> expression) implements DefaultExpression {

    public WrapperExpression {
        requireNonNull(expression);
    }

    @Override
    public String print(PrintContext printContext) {
        return expression.print(printContext);
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new WrapperExpression(expression.withIncrementedSuffixes(increments));
    }

    @Override
    public boolean isIdentifier() {
        return expression.isIdentifier();
    }
}
