package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonCreator
    public static StructuredQuery of(@JsonProperty("inclusionCriteria") List<List<Criterion>> inclusionCriteria,
                                     @JsonProperty("exclusionCriteria") List<List<Criterion>> exclusionCriteria) {
        if (inclusionCriteria.isEmpty() || inclusionCriteria.stream().allMatch(List::isEmpty)) {
            throw new IllegalArgumentException("empty inclusion criteria");
        }
        return new StructuredQuery(inclusionCriteria.stream().map(List::copyOf).collect(Collectors.toUnmodifiableList()),
                exclusionCriteria == null ? List.of(List.of()) : exclusionCriteria.stream().map(List::copyOf)
                        .collect(Collectors.toUnmodifiableList()));
    }

    public List<List<Criterion>> getInclusionCriteria() {
        return inclusionCriteria;
    }

    public List<List<Criterion>> getExclusionCriteria() {
        return exclusionCriteria;
    }
}
