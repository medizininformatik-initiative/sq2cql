package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record SuffixedIdentifierExpression(String prefix, int suffix) implements IdentifierExpression {

    public SuffixedIdentifierExpression {
        requireNonNull(prefix);
    }

    public static SuffixedIdentifierExpression of(String prefix, int suffix) {
        return new SuffixedIdentifierExpression(prefix, suffix);
    }

    @Override
    public String print(PrintContext printContext) {
        return suffix == 0
                ? SAFE_CHARS_PATTERN.matcher(prefix).matches() ? prefix : "\"%s\"".formatted(prefix)
                : "\"%s %d\"".formatted(prefix, suffix);
    }

    @Override
    public IdentifierExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new SuffixedIdentifierExpression(prefix, suffix + increments.getOrDefault(prefix, 0));
    }

    @Override
    public Map<String, Integer> suffixes() {
        return Map.of(prefix, suffix);
    }

    @Override
    public String unquotedIdentifier() {
        return "%s %d".formatted(prefix, suffix);
    }
}
