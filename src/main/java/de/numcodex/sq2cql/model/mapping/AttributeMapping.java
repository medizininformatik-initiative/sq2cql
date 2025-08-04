package de.numcodex.sq2cql.model.mapping;

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
public record AttributeMapping(TermCode key, List<AttributeTreeNode> components) {

    public AttributeMapping {
        requireNonNull(key);
        requireNonNull(components);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("key") JsonNode key,
                                      @JsonProperty("components") List<AttributeTreeNode> components) {
        return new AttributeMapping(TermCode.fromJsonNode(key), components);
    }

}
