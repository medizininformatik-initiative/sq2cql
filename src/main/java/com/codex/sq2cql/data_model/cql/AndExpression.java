package com.codex.sq2cql.data_model.cql;

import com.codex.sq2cql.data_model.common.LogicalOperator;

import java.util.ArrayList;
import java.util.List;


// Expression1 AND Expression2 AND Expression ... AND ExpressionN
public class AndExpression implements BooleanExpression{
    private List<BooleanExpression> expressions = new ArrayList<>();
    private final LogicalOperator andOperator = LogicalOperator.AND;

    public AndExpression() {}

    public AndExpression(List<BooleanExpression> expressionsToBeConjunct) {
        expressions = expressionsToBeConjunct;
    }

    public void addExpression(BooleanExpression expression)
    {
        expressions.add(expression);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

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
                    result.append("(%s) %s\n".formatted(currentExpression.toString(), andOperator.toString()));
                }
            }
        }
        return result.toString();
    }
}
