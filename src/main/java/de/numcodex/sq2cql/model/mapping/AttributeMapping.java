package de.numcodex.sq2cql.model.mapping;

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
public record AttributeMapping(TermCode key, AttributeTreeNode composition) {

    public AttributeMapping {
        requireNonNull(key);
        requireNonNull(composition);
    }

    public static AttributeMapping of(@JsonProperty("key") TermCode key,
                                      @JsonProperty("composition") AttributeTreeNode composition) {
        return new AttributeMapping(key, composition);
    }

    @JsonCreator
    public static AttributeMapping of(@JsonProperty("key") JsonNode key,
                                      @JsonProperty("composition") AttributeTreeNode composition) {
        return new AttributeMapping(TermCode.fromJsonNode(key), composition);
    }

}
