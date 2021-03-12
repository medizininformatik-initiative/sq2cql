package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Objects;

public final class IdentifierExpression implements BooleanExpression {

    private final String identifier;

    private IdentifierExpression(String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public static IdentifierExpression of(String identifier) {
        return new IdentifierExpression(identifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return identifier;
    }
}

