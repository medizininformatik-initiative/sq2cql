package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode concept;
    private final String resourceType;
    private final String valueFhirPath;
    private final List<Modifier> fixedCriteria;

    private Mapping(TermCode concept, String resourceType, String valueFhirPath, List<Modifier> fixedCriteria) {
        this.concept = Objects.requireNonNull(concept);
        this.resourceType = Objects.requireNonNull(resourceType);
        this.valueFhirPath = valueFhirPath;
        this.fixedCriteria = Objects.requireNonNull(fixedCriteria);
    }

    public static Mapping of(TermCode concept, String resourceType) {
        return new Mapping(Objects.requireNonNull(concept), Objects.requireNonNull(resourceType), null, List.of());
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode concept,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("fixedCriteria") Modifier... fixedCriteria) {
        return new Mapping(Objects.requireNonNull(concept), Objects.requireNonNull(resourceType), valueFhirPath,
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

    public TermCode getTermCode() {
        return concept;
    }

    public Optional<String> getValueFhirPath() {
        return Optional.ofNullable(valueFhirPath);
    }
}
