package com.codex.sq2cql.data_model.structured_query;

import java.util.List;

public class Query {
    private List<List<Criterion>> inclusionCriteria;
    private List<List<Criterion>> exclusionCriteria;

    public Query(List<List<Criterion>> cnf) {
        inclusionCriteria = cnf;
    }

    public List<List<Criterion>> getInclusionCriteria() {
        return inclusionCriteria;
    }

    public List<List<Criterion>> getExclusionCriteria() {
        return exclusionCriteria;
    }
}
