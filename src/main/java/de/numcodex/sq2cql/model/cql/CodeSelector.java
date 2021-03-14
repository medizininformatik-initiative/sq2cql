package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class CodeSelector implements TermExpression {

    private final String code;
    private final String codeSystemIdentifier;

    private CodeSelector(String code, String codeSystemIdentifier) {
        this.code = Objects.requireNonNull(code);
        this.codeSystemIdentifier = Objects.requireNonNull(codeSystemIdentifier);
    }

    public static CodeSelector of(String code, String codeSystemIdentifier) {
        return new CodeSelector(code, codeSystemIdentifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return "Code '%s' from %s".formatted(code, codeSystemIdentifier);
    }
}
