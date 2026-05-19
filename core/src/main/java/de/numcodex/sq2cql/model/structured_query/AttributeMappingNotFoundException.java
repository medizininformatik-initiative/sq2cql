package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;

public class AttributeMappingNotFoundException extends TranslationException {

    public AttributeMappingNotFoundException(TermCode termCode) {
        super("Mapping for concept with system `%s`, code `%s` and display `%s`".formatted(
                termCode.system(), termCode.code(), termCode.display()));
    }
}
