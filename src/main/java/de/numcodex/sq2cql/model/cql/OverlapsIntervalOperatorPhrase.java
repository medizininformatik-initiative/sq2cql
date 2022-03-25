package de.numcodex.sq2cql.model.cql;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.PrintContext;

public record OverlapsIntervalOperatorPhrase(Expression leftInterval,
                                             Expression rightInterval) implements
    BooleanExpression {

  public OverlapsIntervalOperatorPhrase {
    requireNonNull(leftInterval);
    requireNonNull(rightInterval);
  }

  public static OverlapsIntervalOperatorPhrase of(Expression leftInterval,
      Expression rightInterval) {
    return new OverlapsIntervalOperatorPhrase(leftInterval, rightInterval);
  }

  @Override
  public String print(PrintContext printContext) {
    return "%s overlaps %s".formatted(leftInterval.print(printContext),
        rightInterval.print(printContext));
  }

}