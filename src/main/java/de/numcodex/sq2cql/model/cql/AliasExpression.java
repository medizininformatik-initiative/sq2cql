package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
public record AliasExpression(String identifier) implements Expression {

    public AliasExpression {
        requireNonNull(identifier);
    }

    public static AliasExpression of(String identifier) {
        return new AliasExpression(identifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return identifier;
    }
}
