package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

public record ElementExtractorExpression(Expression expression) implements
    Expression {

  public static ElementExtractorExpression of(Expression expression) {
    return new ElementExtractorExpression(expression);
  }

  @Override
  public String print(PrintContext printContext) {
    return """
        singleton from (
        \t%s)""".formatted(expression.print(printContext));
  }
}
