package de.numcodex.sq2cql.model.mapping;

import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.*;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import org.hl7.fhir.r4.model.Period;

import java.util.List;

public class PeriodAttributeComponent extends AttributeComponent<Period, PeriodAttributeComponent.Operator> {

    public PeriodAttributeComponent(String path, Mapping.Cardinality cardinality, Operator operator, List<Period> values) {
        super(path, cardinality, operator, values);
    }

    private Expression<?> exprForSingleValue(AttributeFilter attributeFilter, String elementIdentifier, Period value) {
        var invocationExpr = InvocationExpression.of(
                StandardIdentifierExpression.of(elementIdentifier),
                path()
        );
        return switch (operator()) {
            case BEFORE -> ComparatorExpression.of(
                    invocationExpr,
                    Comparator.LESS_THAN,
                    DateExpression.of(value.get)
            )
        }
    }

    @Override
    public Expression<?> toExpr(AttributeFilter attributeFilter, String elementIdentifier, Period value) {
        return switch (values().size()) {
            case 0 -> {
                if (value == null)
                    throw new IllegalArgumentException("An value has to be provided if the attribute component has none of its own");
                yield ComparatorExpression.of(
                        InvocationExpression.of(
                                StandardIdentifierExpression.of(elementIdentifier),
                                path()
                        ),
                        Comparator.EQUIVALENT,
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
                        Comparator.EQUIVALENT,
                        QuantityExpression.of(value.getValue(), value.getCode())
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

    public enum Operator {
        BEFORE, AFTER, UNTIL, FROM, IN, CONTAINS, OVERLAPS
    }
}
