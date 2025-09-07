package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import de.numcodex.sq2cql.model.structured_query.Modifier;
import org.hl7.fhir.instance.model.api.IBaseDatatype;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.SIMPLE_NAME,
        property = "_type"
)
public sealed interface AttributeTreeNode<T extends IBaseDatatype> permits AttributeComponent, ContextGroup {

    Expression<?> toExpr(AttributeFilter attributeFilter, String elementIdentifier, T value);

}
