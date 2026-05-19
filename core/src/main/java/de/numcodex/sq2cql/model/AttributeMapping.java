package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Lorenz Rosenau
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(List<String> types, TermCode key, String path, String referenceTargetType,
                               Mapping.Cardinality cardinality) {

    public AttributeMapping {
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("expected types");
        }
        types = List.copyOf(types);
        requireNonNull(key);
        requireNonNull(path);
        cardinality = cardinality == null ? Mapping.Cardinality.SINGLE : cardinality;
    }

    public static AttributeMapping of(List<String> types, JsonNode key, String path, String referenceTargetType) {
        return new AttributeMapping(types, TermCode.fromJsonNode(key), path, referenceTargetType, Mapping.Cardinality.SINGLE);
    }

    public static AttributeMapping of(List<String> types, TermCode key, String path) {
        return new AttributeMapping(types, key, path, null, Mapping.Cardinality.SINGLE);
    }
}
