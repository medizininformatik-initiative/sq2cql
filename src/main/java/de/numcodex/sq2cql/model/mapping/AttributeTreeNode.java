package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.SIMPLE_NAME,
        property = "_type"
)
public sealed interface AttributeTreeNode permits ContextGroup, ReferenceGroup, AttributeComponent {

}
