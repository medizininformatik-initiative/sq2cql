package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record NumericAttributeFilter(TermCode attributeCode,
                                     Comparator comparator,
                                     BigDecimal value,
                                     String unit) implements AttributeFilter {

    public NumericAttributeFilter {
        requireNonNull(attributeCode);
        requireNonNull(comparator);
        requireNonNull(value);
    }

    /**
     * Returns a {@code NumericAttributeFilter}.
     *
     * @param attributeCode the code identifying the attribute
     * @param comparator    the comparator that should be used in the value comparison
     * @param value         the value that should be used in the value comparison
     * @return the {@code NumericAttributeFilter}
     */
    public static NumericAttributeFilter of(TermCode attributeCode, Comparator comparator, BigDecimal value) {
        return new NumericAttributeFilter(attributeCode, comparator, value, null);
    }

    /**
     * Returns a {@code NumericAttributeFilter}.
     *
     * @param attributeCode the code identifying the attribute
     * @param comparator    the comparator that should be used in the value comparison
     * @param value         the value that should be used in the value comparison
     * @param unit          the unit of the value
     * @return the {@code NumericAttributeFilter}
     */
    public static NumericAttributeFilter of(TermCode attributeCode, Comparator comparator, BigDecimal value, String unit) {
        return new NumericAttributeFilter(attributeCode, comparator, value, requireNonNull(unit));
    }

    @Override
    public Modifier toModifier(AttributeMapping attributeMapping) {
        return NumericModifier.of(attributeMapping.path(), comparator, value, unit);
    }
}
