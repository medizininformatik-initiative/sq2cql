package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record DateExpression(LocalDate date) implements DefaultExpression {

    public DateExpression {
        requireNonNull(date);
    }

    public static DateExpression of(LocalDate date) {
        return new DateExpression(date);
    }

    @Override
    public String print(PrintContext printContext) {
        return "@%s".formatted(date);
    }
}
