package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An {@link IdentifierExpression} that consists of a prefix and numerical suffix rather than a simple string.
 * <p>
 * Both prefix and the suffix together form the actual name of the identifier. In case the suffix is zero, the name
 * consists only of the prefix. Otherwise both are separated by a space.
 * <p>
 * The idea is to use that identifiers to ensure identifiers are unique in a library. In case to libraries are merged,
 * the numerical suffix will be incremented using {@link #withIncrementedSuffixes(Map)} in one of the libraries if both
 * contain identifiers with identical prefix.
 *
 * @param prefix the prefix of the name
 * @param suffix the numerical suffix of the name
 */
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
