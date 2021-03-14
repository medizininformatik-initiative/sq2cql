package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.common.Comparator;

import java.util.Objects;

public final class ComparatorExpression implements BooleanExpression {

    private final Expression leftSide;
    private final Comparator comparator;
    private final Expression rightSide;

    private ComparatorExpression(Expression leftSide, Comparator comparator, Expression rightSide) {
        this.leftSide = Objects.requireNonNull(leftSide);
        this.comparator = Objects.requireNonNull(comparator);
        this.rightSide = Objects.requireNonNull(rightSide);
    }

    public static ComparatorExpression of(Expression leftSide, Comparator comparator, Expression rightSide) {
        return new ComparatorExpression(leftSide, comparator, rightSide);
    }

    @Override
    public String print(PrintContext printContext) {
        return "(%s) %s (%s)".formatted(leftSide.print(printContext), comparator, rightSide.print(printContext));
    }
}
