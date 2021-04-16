package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;

/**
 * @author Alexander Kiel
 */
public class MappingNotFoundException extends TranslationException {

    private final TermCode concept;

    public MappingNotFoundException(TermCode concept) {
        super("Mapping for concept with system `%s`, code `%s` and display `%s` not found."
                .formatted(concept.getSystem(), concept.getCode(), concept.getDisplay()));
        this.concept = concept;
    }

    public TermCode getConcept() {
        return concept;
    }
}
