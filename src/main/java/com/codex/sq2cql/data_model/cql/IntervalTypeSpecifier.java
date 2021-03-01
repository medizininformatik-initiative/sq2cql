package com.codex.sq2cql.data_model.cql;

public class IntervalTypeSpecifier {
    private final TypeSpecifier typeSpecifier;

    public IntervalTypeSpecifier(TypeSpecifier typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    @Override
    public String toString()
    {
        return "Interval<%s>".formatted(typeSpecifier);
    }
}
