package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class RetrieveExpression implements Expression {

    private final String resourceType;
    private final Expression terminology;

    private RetrieveExpression(String resourceType, Expression terminology) {
        this.resourceType = Objects.requireNonNull(resourceType);
        this.terminology = Objects.requireNonNull(terminology);
    }

    public static RetrieveExpression of(String resourceType, Expression terminology) {
        return new RetrieveExpression(resourceType, terminology);
    }

    public String getResourceType() {
        return resourceType;
    }

    @Override
    public String print(PrintContext printContext) {
        return "[%s: %s]".formatted(resourceType, terminology.print(printContext));
    }
}
