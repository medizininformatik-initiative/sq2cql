package de.numcodex.sq2cql.model.cql;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.PrintContext;

public record ReturnClause(Expression expression) {

  public ReturnClause {
    requireNonNull(expression);
  }

  public static ReturnClause of(Expression expression) {
    return new ReturnClause(expression);
  }

  public String toCql(PrintContext printContext) {
    return "return " + expression.print(printContext.resetPrecedence().increase());
  }
}
