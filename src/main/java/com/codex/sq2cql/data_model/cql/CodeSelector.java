package com.codex.sq2cql.data_model.cql;

public class CodeSelector implements TermExpression{
    private final String code;
    private final String codesystemIdentifier;
    private String displayClause;


    public CodeSelector(String code, String codesystemIdentifier) {
        this.code = code;
        this.codesystemIdentifier = codesystemIdentifier;
    }

    @Override
    public String toString() {
        return "Code '%s' from %s%s".formatted(code, codesystemIdentifier, displayClause == null ?
                "" : " display '%s'".formatted(displayClause));
    }
}
