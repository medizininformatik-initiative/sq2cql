package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;

/**
 * @author Alexander Kiel
 */
public class MappingNotFoundException extends TranslationException {

    private final TermCode termCode;

    public MappingNotFoundException(TermCode termCode) {
        super("Mapping for concept with system `%s`, code `%s` and display `%s` not found."
                .formatted(termCode.system(), termCode.code(), termCode.display()));
        this.termCode = termCode;
    }

    public TermCode getTermCode() {
        return termCode;
    }
}
