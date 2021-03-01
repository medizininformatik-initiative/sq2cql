package com.codex.sq2cql.data_model.cql;

public class ListTypeSpecifier  implements TypeSpecifier {
    private final TypeSpecifier typeSpecifier;

    public ListTypeSpecifier(TypeSpecifier typeSpecifier) {
        this.typeSpecifier = typeSpecifier;
    }

    @Override
    public String toString() {
        return "List<%s>".formatted(typeSpecifier);
    }

}
