package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record ExpressionDefinition(IdentifierExpression identifier, Expression expression) implements Statement {

    public ExpressionDefinition {
        requireNonNull(identifier);
        requireNonNull(expression);
    }

    public static ExpressionDefinition of(String identifier, Expression expression) {
        return new ExpressionDefinition(IdentifierExpression.of(identifier), expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        var newPrintContext = printContext.increase();
        return "define %s:\n%s%s".formatted(identifier.print(printContext), newPrintContext.getIndent(),
                expression.print(newPrintContext));
    }
}
