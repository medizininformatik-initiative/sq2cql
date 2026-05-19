package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record IntervalSelector(Expression<?> intervalStart, Expression<?> intervalEnd) implements DefaultExpression {

    public IntervalSelector {
        requireNonNull(intervalStart);
        requireNonNull(intervalEnd);
    }

    public static IntervalSelector of(Expression<?> intervalStart, Expression<?> intervalEnd) {
        return new IntervalSelector(intervalStart, intervalEnd);
    }

    @Override
    public String print(PrintContext printContext) {
        return "Interval[%s, %s]".formatted(intervalStart.print(printContext), intervalEnd.print(printContext));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new IntervalSelector(intervalStart.withIncrementedSuffixes(increments),
                intervalEnd.withIncrementedSuffixes(increments));
    }
}
