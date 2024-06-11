package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
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

    private final ContextualTermCode key;
    private final String resourceType;
    private final String valueFhirPath;
    private final String valueType;
    private final List<Modifier> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final String timeRestrictionFhirPath;
    private final TermCode primaryCode;
    private final String termCodeFhirPath;

    public Mapping(ContextualTermCode key, String resourceType, String valueFhirPath, String valueType, List<Modifier> fixedCriteria,
                   Map<TermCode, AttributeMapping> attributeMappings, String timeRestrictionFhirPath, TermCode primaryCode, String termCodeFhirPath) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.valueFhirPath = valueFhirPath;
        this.valueType = valueType;
        this.fixedCriteria = fixedCriteria;
        this.attributeMappings = attributeMappings;
        this.timeRestrictionFhirPath = timeRestrictionFhirPath;
        this.primaryCode = primaryCode;
        this.termCodeFhirPath = termCodeFhirPath;
    }

    public static Mapping of(ContextualTermCode key, String resourceType) {
        return new Mapping(key, resourceType, "value", null, List.of(), Map.of(), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, String valueFhirPath) {
        return new Mapping(key, resourceType, valueFhirPath, null, List.of(), Map.of(), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, String valueFhirPath, String valueType) {
        return new Mapping(key, resourceType, valueFhirPath, valueType, List.of(), Map.of(), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, String valueFhirPath, String valueType, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath, valueType,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, String valueFhirPath, String valueType, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings, String timeRestrictionFhirPath) {
        return new Mapping(key, resourceType, valueFhirPath == null ? "value" : valueFhirPath, valueType,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))), timeRestrictionFhirPath, null, null);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("context") TermCode context,
                             @JsonProperty("key") TermCode key,
                             @JsonProperty("resourceType") String resourceType,
                             @JsonProperty("valueFhirPath") String valueFhirPath,
                             @JsonProperty("valueType") String valueType,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributeFhirPaths") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestrictionFhirPath") String timeRestrictionFhirPath,
                             @JsonProperty("primaryCode") TermCode primaryCode,
                             @JsonProperty("termCodeFhirPath") String termCodeFhirPath) {
        var contextualTermCode = ContextualTermCode.of(context, key);
        return new Mapping(contextualTermCode,
                requireNonNull(resourceType, "missing JSON property: resourceType"),
                valueFhirPath == null ? "value" : valueFhirPath,
                valueType,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))),
                timeRestrictionFhirPath,
                primaryCode,
                termCodeFhirPath);
    }

    public ContextualTermCode key() {
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

    public Optional<String> timeRestrictionFhirPath() {
        return Optional.ofNullable(timeRestrictionFhirPath);
    }

    /**
     * Returns the primary code of this mapping. The primary code is used in the retrieve CQL
     * expression to identify the resources of interest. The path for the primary code path is
     * implicitly given in CQL but can be looked up
     * <a href="https://github.com/cqframework/clinical_quality_language/blob/master/Src/java/quick/src/main/resources/org/hl7/fhir/fhir-modelinfo-4.0.1.xml">here</a>
     *
     * @return the primary code of this mapping or the key if no primary code is defined
     */
    public TermCode primaryCode() {
        // TODO: decouple key and primary code. The key should only be used for the mapping lookup.
        return primaryCode == null ? key.termCode() : primaryCode;
    }

    public String termCodeFhirPath() {
        return termCodeFhirPath;
    }
}
