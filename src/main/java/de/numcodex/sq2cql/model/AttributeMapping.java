package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import de.numcodex.sq2cql.model.common.TermCode;

import static java.util.Objects.requireNonNull;

/**
 * @author Lorenz Rosenau
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(String type, TermCode key, String path, String referenceTargetType) {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(path);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("attributeType") String type,
                                      @JsonProperty("attributeKey") JsonNode key,
                                      @JsonProperty("attributePath") String path,
                                      @JsonProperty("attributeReferenceTargetType") String referenceTargetType) {
        return new AttributeMapping(type, TermCode.fromJsonNode(key), path, referenceTargetType);
    }

    public static AttributeMapping of(String type, TermCode key, String path) {
        return new AttributeMapping(type, key, path, null);
    }

    public static AttributeMapping of(String type, TermCode key, String path, String referenceTargetType) {
        return new AttributeMapping(type, key, path, referenceTargetType);
    }
}
