package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.numcodex.sq2cql.model.common.TermCode;

import static java.util.Objects.requireNonNull;

/**
 * @author Lorenz Rosenau
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeMapping(String type, TermCode key, String path) {

    public AttributeMapping {
        requireNonNull(type);
        requireNonNull(key);
        requireNonNull(path);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("attributeType") String type,
                                      @JsonProperty("attributeKey") TermCode key,
                                      @JsonProperty("attributeFhirPath") String path) {
        return new AttributeMapping(type, key, path);
    }
}
