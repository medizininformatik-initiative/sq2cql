package de.numcodex.sq2cql.model.mapping;

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
    private final PathMapping valueMapping;
    private final List<Modifier> fixedCriteria;
    private final Map<TermCode, AttributeMapping> attributeMappings;
    private final TimeRestrictionMapping timeRestrictionMapping;
    private final TermCode primaryCode;
    private final PathMapping termCodeMapping;

    public Mapping(ContextualTermCode key, String resourceType, PathMapping valueMapping, List<Modifier> fixedCriteria,
                   Map<TermCode, AttributeMapping> attributeMappings, TimeRestrictionMapping timeRestrictionMapping, TermCode primaryCode, PathMapping termCodeMapping) {
        this.key = requireNonNull(key);
        this.resourceType = requireNonNull(resourceType);
        this.valueMapping = valueMapping;
        this.fixedCriteria = fixedCriteria;
        this.attributeMappings = attributeMappings;
        this.timeRestrictionMapping = timeRestrictionMapping;
        this.primaryCode = primaryCode;
        this.termCodeMapping = termCodeMapping;
    }

    public static Mapping of(ContextualTermCode key, String resourceType) {
        return new Mapping(key, resourceType, null, List.of(), Map.of(), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, PathMapping valueMapping) {
        return new Mapping(key, resourceType, valueMapping, List.of(), Map.of(), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, PathMapping valueMapping, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings) {
        return new Mapping(key, resourceType, valueMapping,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))), null, null, null);
    }

    public static Mapping of(ContextualTermCode key, String resourceType, PathMapping valueMapping, List<Modifier> fixedCriteria, List<AttributeMapping> attributeMappings, TimeRestrictionMapping timeRestriction) {
        return new Mapping(key, resourceType, valueMapping,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))), timeRestriction, null, null);
    }

    @JsonCreator
    public static Mapping of(@JsonProperty("context") TermCode context,
                             @JsonProperty("key") TermCode key,
                             @JsonProperty("resourceType") String resourceType,
                             @JsonProperty("value") PathMapping valueMapping,
                             @JsonProperty("fixedCriteria") List<Modifier> fixedCriteria,
                             @JsonProperty("attributes") List<AttributeMapping> attributeMappings,
                             @JsonProperty("timeRestriction") TimeRestrictionMapping timeRestriction,
                             @JsonProperty("primaryCode") TermCode primaryCode,
                             @JsonProperty("termCode") PathMapping termCodeMapping) {

        if (valueMapping != null && valueMapping.types().size() > 1) {
            throw new IllegalArgumentException("Unsupported `value` mapping with multiple types.");
        }
        if (termCodeMapping != null && termCodeMapping.types().size() > 1) {
            throw new IllegalArgumentException("Unsupported `termCode` mapping with multiple types.");
        }
        var contextualTermCode = ContextualTermCode.of(context, key);
        return new Mapping(contextualTermCode,
                requireNonNull(resourceType, "missing JSON property: resourceType"),
                valueMapping,
                fixedCriteria == null ? List.of() : List.copyOf(fixedCriteria),
                (attributeMappings == null ? Map.of() : attributeMappings.stream()
                        .collect(Collectors.toMap(AttributeMapping::key, Function.identity()))),
                timeRestriction,
                primaryCode,
                termCodeMapping);
    }

    public ContextualTermCode key() {
        return key;
    }

    public String resourceType() {
        return resourceType;
    }

    public Optional<PathMapping> valueMapping() {
        return Optional.ofNullable(valueMapping);
    }

    public List<Modifier> fixedCriteria() {
        return fixedCriteria;
    }

    public Map<TermCode, AttributeMapping> attributeMappings() {
        return attributeMappings;
    }

    public Optional<TimeRestrictionMapping> timeRestrictionMapping() {
        return Optional.ofNullable(timeRestrictionMapping);
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

    public Optional<PathMapping> termCodeMapping() {
        return Optional.ofNullable(termCodeMapping);
    }

    public enum Cardinality {

        @JsonProperty("single")
        SINGLE,

        @JsonProperty("many")
        MANY
    }

    /**
     * Mapping information for a path including it's type.
     *
     * @param path  the FHIRPath path to the value to extract
     * @param types the possible types that extracted values can have
     */
    public record PathMapping(String path, List<Type> types, Cardinality cardinality) {

        public PathMapping {
            requireNonNull(path);
            if (types == null || types.isEmpty()) {
                throw new IllegalArgumentException("Path mapping without types.");
            }
            types = List.copyOf(types);
            cardinality = cardinality == null ? Cardinality.SINGLE : cardinality;
        }

        public static PathMapping of(String fhirPath, Type... types) {
            return new PathMapping(fhirPath, List.of(types), Cardinality.SINGLE);
        }

        public enum Type {

            @JsonProperty("code")
            CODE("code"),

            @JsonProperty("Coding")
            CODING("Coding"),

            @JsonProperty("CodeableConcept")
            CODEABLE_CONCEPT("CodeableConcept"),

            @JsonProperty("date")
            DATE("date"),

            @JsonProperty("dateTime")
            DATE_TIME("dateTime"),

            @JsonProperty("Period")
            PERIOD("Period"),

            @JsonProperty("Quantity")
            QUANTITY("Quantity");

            private final String fhirTypeName;

            Type(String fhirTypeName) {
                this.fhirTypeName = fhirTypeName;
            }

            public String fhirTypeName() {
                return fhirTypeName;
            }
        }
    }

    /**
     * Mapping information for a path including it's type.
     *
     * @param path  the FHIRPath path to the value to extract
     * @param types the possible types that extracted values can have
     */
    public record TimeRestrictionMapping(String path, List<Type> types, Cardinality cardinality) {

        public TimeRestrictionMapping {
            requireNonNull(path);
            if (types.isEmpty()) {
                throw new IllegalArgumentException("at least one type required");
            }
            types = List.copyOf(types);
            cardinality = cardinality == null ? Cardinality.SINGLE : cardinality;
        }

        public static TimeRestrictionMapping of(String fhirPath, Type... types) {
            return new TimeRestrictionMapping(fhirPath, List.of(types), Cardinality.SINGLE);
        }

        public enum Type {

            @JsonProperty("date")
            DATE,

            @JsonProperty("dateTime")
            DATE_TIME,

            @JsonProperty("Period")
            PERIOD
        }
    }
}
