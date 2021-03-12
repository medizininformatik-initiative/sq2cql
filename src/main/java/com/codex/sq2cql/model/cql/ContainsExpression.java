package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class ContainsExpression implements BooleanExpression {

    private final Expression leftSide;
    private final Expression rightSide;

    private ContainsExpression(Expression leftSide, Expression rightSide) {
        this.leftSide = Objects.requireNonNull(leftSide);
        this.rightSide = rightSide;
    }

    public static ContainsExpression of(Expression leftSide, Expression rightSide) {
        return new ContainsExpression(leftSide, rightSide);
    }

    @Override
    public String print(PrintContext printContext) {
        return "(%s) contains (%s)".formatted(leftSide.print(printContext), rightSide.print(printContext));
    }
}
