package de.numcodex.sq2cql.model.mapping;

import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.*;
import de.numcodex.sq2cql.model.structured_query.AttributeFilter;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import java.util.List;
import java.util.Set;

public class CodingAttributeComponent<T extends IBaseDatatype> extends AttributeComponent<T, CodingAttributeComponent.Operator> {

    public CodingAttributeComponent(String path, Mapping.Cardinality cardinality, Operator operator, String valueType, List<T> values) {
        super(path, cardinality, operator, valueType, values);
    }

    private ComparatorExpression codingToExpr(String elementIdentifier, Coding value) {
        var op = switch (operator()) {
            case EQUAL -> Comparator.EQUAL;
            case EQUIVALENT -> Comparator.EQUIVALENT;
        };
        return ComparatorExpression.of(
                InvocationExpression.of(
                        StringLiteralExpression.of(elementIdentifier),
                        path()
                ),
                op,
                CodeSelector.of(value.getSystem(), value.getCode())
        );
    }

    private Expression<?> codeableConceptToExpr(String elementIdentifier, CodeableConcept value) {
        var codings = value.getCoding();
        return switch (codings.size()) {
            case 0 -> throw new IllegalArgumentException("Provided CodeableConcept has to contain at least one Coding");
            case 1 -> codingToExpr(elementIdentifier, codings.getFirst());
            default -> codings.stream()
                    .map(c -> (DefaultExpression) ComparatorExpression.of(
                            StandardIdentifierExpression.of(elementIdentifier),
                            Comparator.EQUIVALENT,
                            CodeSelector.of(c.getSystem(), c.getCode())
                    )).reduce(OrExpression::of).get();
        };
    }

    private Expression<?> valueToExpr(String elementIdentifier, IBaseDatatype value) {
        if (value instanceof Coding) return codingToExpr(elementIdentifier, (Coding) value);
        else return codeableConceptToExpr(elementIdentifier, (CodeableConcept) value);
    }

    public Expression<?> toExpr(AttributeFilter attributeFilter, String elementIdentifier, T value) {
        if (values().isEmpty()) {
            if (value == null)
                throw new IllegalArgumentException("A value has to be provided if the attribute component has none of its own");
            return valueToExpr(elementIdentifier, value);
        } else {
            if (values().size() == 1) {
                return valueToExpr(elementIdentifier, values().getFirst());
            } else {
                return MembershipExpression.in(
                        InvocationExpression.of(
                                StandardIdentifierExpression.of(elementIdentifier),
                                path()
                        ),
                        ListSelector.of(values().stream().map(v -> (DefaultExpression) valueToExpr(elementIdentifier, v)).toList())
                );
            }
        }
    }

    @Override
    public Set<String> supportedValueTypes() {
        return Set.of("Coding", "CodeableConcept");
    }

    public enum Operator {
        EQUAL, EQUIVALENT
    }
}
