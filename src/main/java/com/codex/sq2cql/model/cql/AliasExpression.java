package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

/**
 * @author Alexander Kiel
 */
public final class AliasExpression implements Expression {

    private final String identifier;

    private AliasExpression(String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public static AliasExpression of(String identifier) {
        return new AliasExpression(identifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return identifier;
    }
}
