package com.codex.sq2cql.data_model.structured_query;

import com.codex.sq2cql.data_model.common.TermCode;

public class Criterion {
    public Criterion(TermCode concept) {
        this.concept = concept;
    }

    private final TermCode concept;
    private TimeRestriction timeRestriction;

    public TermCode getTermCode() {
        return concept;
    }

    public TimeRestriction getTimeRestriction() {
        return timeRestriction;
    }

    public void setTimeRestriction(TimeRestriction timeRestriction) {
        this.timeRestriction = timeRestriction;
    }
}
