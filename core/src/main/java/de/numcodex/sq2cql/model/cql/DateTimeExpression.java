package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record DateTimeExpression(LocalDate date) implements DefaultExpression {

    public DateTimeExpression {
        requireNonNull(date);
    }

    public static DateTimeExpression of(LocalDate date) {
        return new DateTimeExpression(date);
    }

    @Override
    public String print(PrintContext printContext) {
        return "@%sT".formatted(date);
    }
}
