package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;

public record ContextualTermCode(TermCode context, TermCode termCode) {

    public static ContextualTermCode of(TermCode context, TermCode termCode) {
        return new ContextualTermCode(context, termCode);
    }
}
