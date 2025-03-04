package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.common.TermCode;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Lorenz Rosenau
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(List<String> types, TermCode key, String path, String referenceTargetType) {

    public AttributeMapping {
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("expected types");
        }
        types = List.copyOf(types);
        requireNonNull(key);
        requireNonNull(path);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("types") List<String> types,
                                      @JsonProperty("key") JsonNode key,
                                      @JsonProperty("path") String path,
                                      @JsonProperty("referenceTargetType") String referenceTargetType) {
        return new AttributeMapping(types, TermCode.fromJsonNode(key), path, referenceTargetType);
    }

    public static AttributeMapping of(List<String> types, TermCode key, String path) {
        return new AttributeMapping(types, key, path, null);
    }
}
