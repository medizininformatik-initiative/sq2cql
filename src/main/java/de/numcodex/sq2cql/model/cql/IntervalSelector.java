package de.numcodex.sq2cql.model.cql;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.PrintContext;

public record IntervalSelector (Expression intervalStart, Expression intervalEnd) implements TermExpression{

  public IntervalSelector {
    requireNonNull(intervalStart);
    requireNonNull(intervalEnd);
  }

  public static IntervalSelector of(Expression intervalStart, Expression intervalEnd) {
    return new IntervalSelector(intervalStart, intervalEnd);
  }

  @Override
  public String print(PrintContext printContext) {
    return "Interval[%s, %s]".formatted(intervalStart.print(printContext), intervalEnd.print(printContext));
  }

}
