package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class StringLiteralExpression implements Expression {

    private final String value;

    private StringLiteralExpression(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static StringLiteralExpression of(String value) {
        return new StringLiteralExpression(value);
    }

    @Override
    public String print(PrintContext printContext) {
        return "'%s'".formatted(value);
    }
}
