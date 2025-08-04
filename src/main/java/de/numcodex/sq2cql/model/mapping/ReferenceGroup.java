package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReferenceGroup(String type, String path, List<AttributeTreeNode> components) implements AttributeTreeNode {

    public ReferenceGroup {
        requireNonNull(type);
        requireNonNull(path);
        requireNonNull(components);
        if (components.isEmpty()) throw new IllegalArgumentException("The component list should not be empty");
    }

    @JsonCreator
    public static ReferenceGroup of(
            @JsonProperty("type") String type,
            @JsonProperty("path") String path,
            @JsonProperty("components") List<AttributeTreeNode> components) {
        return new ReferenceGroup(type, path, components);
    }

}
