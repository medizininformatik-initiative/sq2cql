package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Mapping {

    private final TermCode key;
    private final String resourceType;
    private final String valueFhirPath;
    private final String valueType;
    private final List<Modifier> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final String timeRestrictionPath;

    public Mapping(TermCode key, String resourceType, String valueFhirPath, String valueType, List<Modifier> fixedCriteria,
        List<AttributeMapping> attributeMappings, String timeRestrictionPath) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.valueFhirPath = requireNonNull(valueFhirPath);
        this.valueType = valueType;
        this.fixedCriteria = List.copyOf(fixedCriteria);
        this.attributeMappings = (attributeMappings == null ? Map.of() : attributeMappings.stream()
                .collect(Collectors.toMap(AttributeMapping::key, Function.identity())));
        this.timeRestrictionPath = timeRestrictionPath;
    }

    public static Mapping of(TermCode key, String resourceType) {
        return new Mapping(key, resourceType, "value", null, List.of(), List.of(), null);
    }

    public static Mapping of(TermCode concept, String resourceType, String valueFhirPath) {
        return new Mapping(concept, resourceType, valueFhirPath, null, List.of(), List.of(), null);
    }

    public static Mapping of(TermCode concept, String resourceType, String valueFhirPath, String valueType) {
        return new Mapping(concept, resourceType, valueFhirPath, valueType, List.of(), List.of(), null);
    }

    public static Mapping of(TermCode key, String resourceType, String valueFhirPath, String valueType, List<Modifier> fixedCriteria,List<AttributeMapping> attributeMappings) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath, valueType,
            fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
            attributeMappings, null);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("key") TermCode key,
                             @JsonProperty("fhirResourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("valueType") String valueType,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributeSearchParameters") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestrictionPath") String timeRestrictionPath) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath, valueType,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                attributeMappings, timeRestrictionPath);
    }

    public TermCode key() {
        return key;
    }

    public String resourceType() {
        return resourceType;
    }

    public String valueFhirPath() {
        return valueFhirPath;
    }

    public String valueType() {
        return valueType;
    }

    public List<Modifier> fixedCriteria() {
        return fixedCriteria;
    }

    public Map<TermCode, AttributeMapping> attributeMappings() {
        return attributeMappings;
    }

    public Optional<String> timeRestrictionPath() {
        return Optional.ofNullable(timeRestrictionPath);
    }
}
