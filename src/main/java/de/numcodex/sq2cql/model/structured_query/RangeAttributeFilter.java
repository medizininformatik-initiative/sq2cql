package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.AttributeComponent;
import de.numcodex.sq2cql.model.mapping.AttributeMapping;
import de.numcodex.sq2cql.model.common.TermCode;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record RangeAttributeFilter(TermCode attributeCode,
                                   BigDecimal lowerBound,
                                   BigDecimal upperBound,
                                   String unit) implements AttributeFilter {

    public RangeAttributeFilter {
        requireNonNull(attributeCode);
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, null);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, requireNonNull(unit));
    }

    @Override
    public Modifier targetElementToModifier(AttributeComponent attributeComponent) {
        return RangeModifier.of(attributeComponent.path(), lowerBound, upperBound, unit);
    }
}
