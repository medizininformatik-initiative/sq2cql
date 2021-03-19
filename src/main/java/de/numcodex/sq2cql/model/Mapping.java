package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.Objects;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode concept;
    private final String resourceType;

    private Mapping(TermCode concept, String resourceType) {
        this.concept = Objects.requireNonNull(concept);
        this.resourceType = Objects.requireNonNull(resourceType);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("termCode")TermCode concept,
                             @JsonProperty("fhirResourceType") String resourceType) {
        return new Mapping(concept, resourceType);
    }

    public TermCode getConcept() {
        return concept;
    }

    public String getResourceType() {
        return resourceType;
    }
}
