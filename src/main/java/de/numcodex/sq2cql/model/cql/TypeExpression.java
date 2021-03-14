package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class TypeExpression implements Expression {

    public static final int PRECEDENCE = 12;

    private final Expression expression;
    private final String typeSpecifier;

    private TypeExpression(Expression expression, String typeSpecifier) {
        this.expression = Objects.requireNonNull(expression);
        this.typeSpecifier = Objects.requireNonNull(typeSpecifier);
    }

    public static TypeExpression of(Expression expression, String typeSpecifier) {
        return new TypeExpression(expression, typeSpecifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "%s as %s".formatted(this.expression.print(printContext
                .withPrecedence(PRECEDENCE)), typeSpecifier));
    }
}
