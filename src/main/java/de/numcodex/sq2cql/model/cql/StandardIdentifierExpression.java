package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

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
}
