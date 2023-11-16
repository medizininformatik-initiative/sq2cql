package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record ExpressionDefinition(IdentifierExpression name, Expression<?> expression) implements Statement {

    public ExpressionDefinition {
        requireNonNull(name);
        requireNonNull(expression);
    }

    public static ExpressionDefinition of(String name, Expression<?> expression) {
        return new ExpressionDefinition(StandardIdentifierExpression.of(name), expression);
    }

    public static ExpressionDefinition of(IdentifierExpression name, Expression<?> expression) {
        return new ExpressionDefinition(name, expression);
    }

    @Override
    public String print(PrintContext printContext) {
        assert printContext.precedence() == 0;
        var newPrintContext = printContext.increase();
        return "define %s:\n%s%s".formatted(name.print(printContext), newPrintContext.getIndent(),
                expression.print(newPrintContext));
    }

    public Map<String, Integer> suffixes() {
        return name.suffixes();
    }

    public ExpressionDefinition withIncrementedSuffixes(Map<String, Integer> increments) {
        return new ExpressionDefinition(name.withIncrementedSuffixes(increments),
                expression.withIncrementedSuffixes(increments));
    }
}
