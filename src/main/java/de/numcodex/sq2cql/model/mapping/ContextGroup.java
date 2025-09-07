package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.numcodex.sq2cql.model.structured_query.Modifier;

import static java.util.Objects.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public non-sealed class ContextGroup implements AttributeTreeNode {

    private final String path;
    private final List<AttributeTreeNode> components;

    public ContextGroup(String path, List<AttributeTreeNode> components) {
        requireNonNull(path);
        requireNonNull(components);
        if (components.isEmpty()) throw new IllegalArgumentException("The component list should not be empty");
        this.path = path;
        this.components = components;
    }

    @JsonCreator
    public static ContextGroup of(
            String path,
            List<AttributeTreeNode> components) {
        return new ContextGroup(path, components);
    }

    public List<AttributeTreeNode> components() {
        return components;
    }

    public String path() {
        return path;
    }

    @Override
    public Modifier toModifier() {
        return null;
    }
}
