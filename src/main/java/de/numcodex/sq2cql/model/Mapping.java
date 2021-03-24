package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import java.util.List;
import java.util.Objects;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode concept;
    private final String resourceType;
    private final List<Modifier> fixedCriteria;

    private Mapping(TermCode concept, String resourceType, List<Modifier> fixedCriteria) {
        this.concept = concept;
        this.resourceType = resourceType;
        this.fixedCriteria = fixedCriteria;
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode concept,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("fixedCriteria") Modifier... fixedCriteria) {
        return new Mapping(Objects.requireNonNull(concept), Objects.requireNonNull(resourceType),
                fixedCriteria == null ? List.of() : List.of(fixedCriteria));
    }

    public TermCode getConcept() {
        return concept;
    }

    public String getResourceType() {
        return resourceType;
    }

    public List<Modifier> getFixedCriteria() {
        return fixedCriteria;
    }
}
