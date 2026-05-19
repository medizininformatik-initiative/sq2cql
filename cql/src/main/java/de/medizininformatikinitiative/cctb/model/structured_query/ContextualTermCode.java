package de.medizininformatikinitiative.cctb.model.structured_query;

import de.medizininformatikinitiative.cctb.model.common.TermCode;

public record ContextualTermCode(TermCode context, TermCode termCode) {

    public static ContextualTermCode of(TermCode context, TermCode termCode) {
        return new ContextualTermCode(context, termCode);
    }
}
