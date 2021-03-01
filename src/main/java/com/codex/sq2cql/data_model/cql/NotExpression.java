package com.codex.sq2cql.data_model.cql;

import com.codex.sq2cql.data_model.common.LogicalOperator;

public class NotExpression implements BooleanExpression{
    private final LogicalOperator notOperator = LogicalOperator.NOT;
    private final BooleanExpression expression;

    public NotExpression(BooleanExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(notOperator.toString(), expression.toString());
    }
}
