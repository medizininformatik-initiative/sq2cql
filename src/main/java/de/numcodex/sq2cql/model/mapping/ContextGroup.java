package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static java.util.Objects.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ContextGroup(String path, List<AttributeTreeNode> components) implements AttributeTreeNode {

    public ContextGroup {
        requireNonNull(path);
        requireNonNull(components);
        if (components.isEmpty()) throw new IllegalArgumentException("The component list should not be empty");
    }

    @JsonCreator
    public static ContextGroup of(
            String path,
            List<AttributeTreeNode> components) {
        return new ContextGroup(path, components);
    }

}
