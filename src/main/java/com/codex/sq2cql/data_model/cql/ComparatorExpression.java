package com.codex.sq2cql.data_model.cql;

import com.codex.sq2cql.data_model.common.Comparator;

public class ComparatorExpression implements BooleanExpression{
    private final Expression leftSideExpression;
    private final Expression rightSideExpression;
    private final Comparator comparator;

    public ComparatorExpression(Expression leftSideExpression, Comparator comparator, Expression rightSideExpression) {
        this.leftSideExpression = leftSideExpression;
        this.comparator = comparator;
        this.rightSideExpression = rightSideExpression;
    }

    @Override
    public String toString()
    {
        return "(%s) %s (%s)".formatted(leftSideExpression.toString(), comparator.toString(), rightSideExpression.toString());
    }
}
