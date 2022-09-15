package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record IdentifierExpression(String identifier) implements BooleanExpression {

  public IdentifierExpression {
    requireNonNull(identifier);
  }

  public static IdentifierExpression of(String identifier) {
    return new IdentifierExpression(identifier);
  }

  @Override
  public String print(PrintContext printContext) {
    return identifier.matches("([A-Za-z]|'_')([A-Za-z0-9]|'_')*") ? identifier
        : "\"%s\"".formatted(identifier);
  }
}
