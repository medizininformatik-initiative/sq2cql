package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import static java.util.Objects.requireNonNull;


public record StringLiteralExpression(String value) implements DefaultExpression {

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
