package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record StandardIdentifierExpression(String identifier) implements IdentifierExpression {

    public StandardIdentifierExpression {
        requireNonNull(identifier);
    }

    public static IdentifierExpression of(String identifier) {
        return new StandardIdentifierExpression(identifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return SAFE_CHARS_PATTERN.matcher(identifier).matches() ? identifier : "\"%s\"".formatted(identifier);
    }

    @Override
    public IdentifierExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return this;
    }

    @Override
    public String unquotedIdentifier() {
        return identifier;
    }
}
