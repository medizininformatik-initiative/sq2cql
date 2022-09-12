package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record TypeExpression(Expression expression, String typeSpecifier) implements Expression {

    public static final int PRECEDENCE = 12;

    public TypeExpression {
        requireNonNull(expression);
        requireNonNull(typeSpecifier);
    }

    public static TypeExpression of(Expression expression, String typeSpecifier) {
        return new TypeExpression(expression, typeSpecifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "%s as %s".formatted(expression.print(printContext
                .withPrecedence(PRECEDENCE)), typeSpecifier));
    }
}
