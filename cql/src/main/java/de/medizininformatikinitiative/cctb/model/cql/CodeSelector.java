package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record CodeSelector(String code, String codeSystemIdentifier) implements ExpressionTerm<CodeSelector> {

    public CodeSelector {
        requireNonNull(code);
        requireNonNull(codeSystemIdentifier);
    }

    public static CodeSelector of(String code, String codeSystemIdentifier) {
        return new CodeSelector(code, codeSystemIdentifier);
    }

    @Override
    public String print(PrintContext printContext) {
        return "Code '%s' from %s".formatted(code, codeSystemIdentifier);
    }

    @Override
    public CodeSelector withIncrementedSuffixes(Map<String, Integer> increments) {
        return this;
    }
}
