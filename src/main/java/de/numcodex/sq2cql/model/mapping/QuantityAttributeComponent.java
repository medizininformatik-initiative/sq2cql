package de.numcodex.sq2cql.model.mapping;

import de.numcodex.sq2cql.model.cql.ComparatorExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import org.hl7.fhir.r4.model.Quantity;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;

import java.util.List;
import java.util.Set;

public class QuantityAttributeComponent extends AttributeComponent<Quantity, QuantityAttributeComponent.Operator> {

    public QuantityAttributeComponent(String path, Mapping.Cardinality cardinality, Operator operator, String valueType, List<Quantity> values) {
        super(path, cardinality, operator, valueType, values);
    }

    @Override
    public Expression<?> toExpr(AttributeFilter attributeFilter, String elementIdentifier, Quantity value) {
        var op = switch (operator()) {
            case EQUAL -> Comparator.EQUAL;
            case EQUIVALENT -> Comparator.EQUIVALENT;
        };
        return switch (values().size()) {
            case 0 -> {
                if (value == null)
                    throw new IllegalArgumentException("An value has to be provided if the attribute component has none of its own");
                yield ComparatorExpression.of(
                        InvocationExpression.of(
                                StandardIdentifierExpression.of(elementIdentifier),
                                path()
                        ),
                        op,
                        QuantityExpression.of(value.getValue(), value.getCode())
                );
            }
            case 1 -> {
                var v = values().getFirst();
                yield ComparatorExpression.of(
                        InvocationExpression.of(
                                StandardIdentifierExpression.of(elementIdentifier),
                                path()
                        ),
                        op,
                        QuantityExpression.of(v.getValue(), v.getCode())
                );
            }
            default -> MembershipExpression.in(
                    InvocationExpression.of(
                            StandardIdentifierExpression.of(elementIdentifier),
                            path()
                    ),
                    ListSelector.of(values().stream().map(v -> QuantityExpression.of(v.getValue(), v.getCode())).toList())
            );
        };
    }

    @Override
    public Set<String> supportedValueTypes() {
        return Set.of("Quantity");
    }

    public enum Operator {
        EQUAL, EQUIVALENT
    }
}
