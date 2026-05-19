package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

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
