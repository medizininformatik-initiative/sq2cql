package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Mapping(TermCode key, String resourceType, String valueFhirPath, List<Modifier> fixedCriteria,
                      Map<TermCode, AttributeMapping> attributeMappings) {

    public Mapping {
        requireNonNull(key);
        requireNonNull(resourceType);
        requireNonNull(valueFhirPath);
        List.copyOf(fixedCriteria);
        Map.copyOf(attributeMappings);
    }

    public static Mapping of(TermCode key, String resourceType) {
        return new Mapping(key, resourceType, "value", List.of(), Map.of());
    }

    public static Mapping of(TermCode concept, String resourceType, String valueFhirPath) {
        return new Mapping(concept, resourceType, valueFhirPath, List.of(), Map.of());
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributeMappings") List<AttributeMapping> attributeMappings) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity())));
    }
}
