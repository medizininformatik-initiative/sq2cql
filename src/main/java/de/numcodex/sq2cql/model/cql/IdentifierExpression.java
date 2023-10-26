package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public record IdentifierExpression(String identifier) implements BooleanExpression {

  public static final Pattern SAFE_CHARS_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  public IdentifierExpression {
    requireNonNull(identifier);
  }

  public static IdentifierExpression of(String identifier) {
    return new IdentifierExpression(identifier);
  }

  @Override
  public String print(PrintContext printContext) {
    return SAFE_CHARS_PATTERN.matcher(identifier).matches() ? identifier : "\"%s\"".formatted(identifier);
  }
}
