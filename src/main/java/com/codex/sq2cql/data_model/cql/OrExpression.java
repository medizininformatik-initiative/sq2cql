package com.codex.sq2cql.data_model.cql;

import com.codex.sq2cql.data_model.common.LogicalOperator;

import java.util.ArrayList;
import java.util.List;

public class OrExpression implements BooleanExpression{
    // Expression1 OR Expression2 OR Expression ... OR ExpressionN
    private List<BooleanExpression> expressions = new ArrayList<>();
    private final LogicalOperator orOperator = LogicalOperator.OR;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("");

        if(expressions.size() == 1) {
            return "(%s)".formatted(expressions.get(0).toString());
        }
        else
        {
            for(var currentExpression : expressions)
            {
                if(currentExpression == expressions.get(expressions.size()-1)) {
                    result.append("(%s)".formatted(currentExpression.toString()));
                }
                else {
                    result.append("(%s) %s\n".formatted(currentExpression.toString(), orOperator));
                }
            }
        }
        return result.toString();
    }

    public void addExpression(BooleanExpression expression)
    {
        expressions.add(expression);
    }
}

