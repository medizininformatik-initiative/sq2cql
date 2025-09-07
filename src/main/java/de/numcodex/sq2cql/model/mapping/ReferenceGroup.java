package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceGroup extends ContextGroup {

    private final String type;

    public ReferenceGroup(String type, String path, List<AttributeTreeNode> components) {
        super(path, components);
        requireNonNull(type);
        this.type = type;
    }

    @JsonCreator
    public static ReferenceGroup of(
            @JsonProperty("type") String type,
            @JsonProperty("path") String path,
            @JsonProperty("components") List<AttributeTreeNode> components) {
        return new ReferenceGroup(type, path, components);
    }

    public String type() {
        return type;
    }
}
