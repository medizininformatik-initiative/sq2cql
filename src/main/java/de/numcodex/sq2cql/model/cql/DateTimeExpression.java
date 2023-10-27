package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record DateTimeExpression(String dateTime) implements Expression {

    public DateTimeExpression {
        requireNonNull(dateTime);
    }

    public static DateTimeExpression of(String dateTime) {
        return new DateTimeExpression(dateTime);
    }

    @Override
    public String print(PrintContext printContext) {
        return "@%s".formatted(dateTime);
    }
}
