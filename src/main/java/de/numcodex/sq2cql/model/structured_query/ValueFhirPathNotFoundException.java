package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;

public class ValueFhirPathNotFoundException extends TranslationException {

    private final TermCode concept;

    public ValueFhirPathNotFoundException(TermCode concept) {
        super("ValueFhirPath for concept with system `%s`, code `%s` and display `%s` not found."
                .formatted(concept.getSystem(), concept.getCode(), concept.getDisplay()));
        this.concept = concept;
    }

    public TermCode getConcept() {
        return concept;
    }
}