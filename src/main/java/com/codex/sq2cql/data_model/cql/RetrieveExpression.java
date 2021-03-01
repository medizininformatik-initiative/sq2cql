package com.codex.sq2cql.data_model.cql;

import java.util.Optional;

public class RetrieveExpression implements Expression {
    private final NamedTypeSpecifier namedTypeSpecifier;
    private Terminology terminology;
    private ContextIdentifier contextIdentifier;
    private CodePath codePath;


    public RetrieveExpression(NamedTypeSpecifier namedTypeSpecifier, Terminology terminology) {
        this.namedTypeSpecifier = namedTypeSpecifier;
        this.terminology = terminology;
    }

    @Override
    public String toString() {
        return "[%s%s%s]".formatted(contextIdentifier == null ? "" : contextIdentifier.toString() + "->",
                namedTypeSpecifier.toString(),
                terminology == null ? "" : ": %s"
                        .formatted(codePath == null ? "" : codePath.toString() + " in ") + terminology.toString());
    }

    public void setContextIdentifier(ContextIdentifier contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    public void setCodePath(CodePath codePath) {
        this.codePath = codePath;
    }

    public NamedTypeSpecifier getNamedTypeSpecifier() {
        return namedTypeSpecifier;
    }
}
