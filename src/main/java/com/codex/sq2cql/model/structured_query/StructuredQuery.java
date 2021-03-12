package com.codex.sq2cql.model.structured_query;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexander Kiel
 */
public final class StructuredQuery {

    private final List<List<Criterion>> inclusionCriteria;
    private final List<List<Criterion>> exclusionCriteria;

    private StructuredQuery(List<List<Criterion>> inclusionCriteria, List<List<Criterion>> exclusionCriteria) {
        this.inclusionCriteria = inclusionCriteria;
        this.exclusionCriteria = exclusionCriteria;
    }

    public static StructuredQuery of(List<List<Criterion>> inclusionCriteria) {
        return new StructuredQuery(inclusionCriteria.stream().map(List::copyOf).collect(Collectors.toUnmodifiableList()),
                List.of(List.of()));
    }

    public static StructuredQuery of(List<List<Criterion>> inclusionCriteria, List<List<Criterion>> exclusionCriteria) {
        return new StructuredQuery(inclusionCriteria.stream().map(List::copyOf).collect(Collectors.toUnmodifiableList()),
                exclusionCriteria.stream().map(List::copyOf).collect(Collectors.toUnmodifiableList()));
    }

    public List<List<Criterion>> getInclusionCriteria() {
        return inclusionCriteria;
    }

    public List<List<Criterion>> getExclusionCriteria() {
        return exclusionCriteria;
    }
}
