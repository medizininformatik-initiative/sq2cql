package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Mapping(TermCode key, String resourceType, String valueFhirPath, List<Modifier> fixedCriteria) {

    public Mapping {
        requireNonNull(key);
        requireNonNull(resourceType);
        requireNonNull(valueFhirPath);
        requireNonNull(fixedCriteria);
    }

    public static Mapping of(TermCode key, String resourceType) {
        return new Mapping(requireNonNull(key), requireNonNull(resourceType), "value", List.of());
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("fixedCriteria") Modifier... fixedCriteria) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath,
                fixedCriteria == null ? List.of() : List.of(fixedCriteria));
    }

    public Optional<String> getValueFhirPath() {
        return Optional.ofNullable(valueFhirPath);
    }
}
