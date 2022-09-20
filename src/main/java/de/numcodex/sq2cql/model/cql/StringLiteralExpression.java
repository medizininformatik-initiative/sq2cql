package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;


public record StringLiteralExpression(String value) implements ExpressionTerm {

    public StringLiteralExpression {
        requireNonNull(value);
    }

    public static StringLiteralExpression of(String value) {
        return new StringLiteralExpression(value);
    }

    @Override
    public String print(PrintContext printContext) {
        return "'%s'".formatted(value);
    }
}
