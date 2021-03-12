package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;
import com.codex.sq2cql.model.common.Comparator;

import java.util.Objects;

public final class BetweenExpression implements BooleanExpression {

    private final Expression leftSide;
    private final Expression lowerBound;
    private final Expression upperBound;

    private BetweenExpression(Expression leftSide, Expression lowerBound, Expression upperBound) {
        this.leftSide = Objects.requireNonNull(leftSide);
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);
    }

    public static BetweenExpression of(Expression leftSide, Expression comparator, Expression rightSide) {
        return new BetweenExpression(leftSide, comparator, rightSide);
    }

    @Override
    public String print(PrintContext printContext) {
        return "(%s) between (%s) and (%s)".formatted(leftSide.print(printContext), lowerBound.print(printContext),
                upperBound.print(printContext));
    }
}
