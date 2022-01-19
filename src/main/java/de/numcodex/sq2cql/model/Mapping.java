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

    private final TermCode key;
    private final String resourceType;
    private final String valueFhirPath;
    private final List<Modifier> fixedCriteria;

    private Mapping(TermCode key, String resourceType, String valueFhirPath, List<Modifier> fixedCriteria) {
        this.key = Objects.requireNonNull(key);
        this.resourceType = Objects.requireNonNull(resourceType);
        this.valueFhirPath = valueFhirPath;
        this.fixedCriteria = Objects.requireNonNull(fixedCriteria);
    }

    public static Mapping of(TermCode concept, String resourceType) {
        return new Mapping(Objects.requireNonNull(concept), Objects.requireNonNull(resourceType), null, List.of());
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("fixedCriteria") Modifier... fixedCriteria) {
        return new Mapping(Objects.requireNonNull(key), Objects.requireNonNull(resourceType), valueFhirPath,
                fixedCriteria == null ? List.of() : List.of(fixedCriteria));
    }

    public TermCode getKey() {
        return key;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Optional<String> getValueFhirPath() {
        return Optional.ofNullable(valueFhirPath);
    }

    public List<Modifier> getFixedCriteria() {
        return fixedCriteria;
    }
}
