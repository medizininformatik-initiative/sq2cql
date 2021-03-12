package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class ExpressionDefinition implements Statement {

    private final String identifier;
    private final Expression expression;

    private ExpressionDefinition(String identifier, Expression expression) {
        this.identifier = Objects.requireNonNull(identifier);
        this.expression = Objects.requireNonNull(expression);
    }

    public static ExpressionDefinition of(String identifier, Expression expression) {
        return new ExpressionDefinition(identifier, expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public String print(PrintContext printContext) {
        var newPrintContext = printContext.increase();
        return "define %s:\n%s%s".formatted(identifier, newPrintContext.getIndent(),
                expression.print(newPrintContext));
    }
}
