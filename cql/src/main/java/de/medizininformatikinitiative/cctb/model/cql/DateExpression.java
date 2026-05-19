package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

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
