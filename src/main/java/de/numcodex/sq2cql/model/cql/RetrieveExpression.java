package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record RetrieveExpression(String resourceType, Expression terminology) implements Expression {

    public RetrieveExpression {
        requireNonNull(resourceType);
    }

    public static RetrieveExpression of(String resourceType) {
        return new RetrieveExpression(resourceType, null);
    }

    public static RetrieveExpression of(String resourceType, Expression terminology) {
        return new RetrieveExpression(resourceType, terminology);
    }

    @Override
    public String print(PrintContext printContext) {
        if(Objects.isNull(terminology)) {
            return "[%s]".formatted(resourceType);
        }
        return "[%s: %s]".formatted(resourceType, terminology.print(printContext));
    }

    public IdentifierExpression alias() {
        return IdentifierExpression.of(resourceType.substring(0, 1));
    }
}
