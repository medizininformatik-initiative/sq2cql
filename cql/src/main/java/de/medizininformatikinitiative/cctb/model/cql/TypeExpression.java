package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record TypeExpression(Expression<?> expression, String typeSpecifier) implements DefaultExpression {

    public static final int PRECEDENCE = 12;

    public TypeExpression {
        requireNonNull(expression);
        requireNonNull(typeSpecifier);
    }

    public static TypeExpression of(Expression<?> expression, String typeSpecifier) {
        return new TypeExpression(expression, typeSpecifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "%s as %s".formatted(expression.print(printContext
                .withPrecedence(PRECEDENCE)), typeSpecifier));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new TypeExpression(expression.withIncrementedSuffixes(increments), typeSpecifier);
    }
}
