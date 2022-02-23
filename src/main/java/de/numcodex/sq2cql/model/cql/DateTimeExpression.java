package de.numcodex.sq2cql.model.cql;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.PrintContext;

public record DateTimeExpression(String dateTime) implements Expression {

  public DateTimeExpression {
    requireNonNull(dateTime);
  }

  public static DateTimeExpression of(String dateTime) {
    return new DateTimeExpression(dateTime);
  }

  @Override
  public String print(PrintContext printContext) {
    return "@%s".formatted(dateTime);
  }
}
