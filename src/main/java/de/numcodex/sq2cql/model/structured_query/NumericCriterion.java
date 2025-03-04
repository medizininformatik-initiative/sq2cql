package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@code NumericCriterion} will select all patients that have at least one resource represented
 * by that concept and numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class NumericCriterion extends AbstractCriterion<NumericCriterion> {

    private final Comparator comparator;
    private final BigDecimal value;
    private final String unit;

    private NumericCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
                             TimeRestriction timeRestriction, Comparator comparator,
                             BigDecimal value, String unit) {
        super(concept, attributeFilters, timeRestriction);
        this.value = value;
        this.comparator = comparator;
        this.unit = unit;
    }

    /**
     * Returns a {@code NumericCriterion}.
     *
     * @param concept    the concept the criterion represents
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @return the {@code NumericCriterion}
     */
    public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value) {
        return new NumericCriterion(concept, List.of(), null,
                requireNonNull(comparator),
                requireNonNull(value), null);
    }

    /**
     * Returns a {@code NumericCriterion}.
     *
     * @param concept    the concept the criterion represents
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @param unit       the unit of the value
     * @return the {@code NumericCriterion}
     */
    public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
                                      String unit) {
        return new NumericCriterion(concept, List.of(), null, requireNonNull(comparator),
                requireNonNull(value),
                requireNonNull(unit));
    }

    /**
     * Returns a {@code NumericCriterion}.
     *
     * @param concept    the concept the criterion represents
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @return the {@code NumericCriterion}
     */
    public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
                                      TimeRestriction timeRestriction) {
        return new NumericCriterion(concept, List.of(), timeRestriction, requireNonNull(comparator),
                requireNonNull(value), null);
    }

    /**
     * Returns a {@code NumericCriterion}.
     *
     * @param concept    the concept the criterion represents
     * @param comparator the comparator that should be used in the value comparison
     * @param value      the value that should be used in the value comparison
     * @param unit       the unit of the value
     * @return the {@code NumericCriterion}
     */
    public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
                                      String unit, TimeRestriction timeRestriction) {
        return new NumericCriterion(concept, List.of(), timeRestriction, requireNonNull(comparator),
                requireNonNull(value), requireNonNull(unit));
    }

    @Override
    public NumericCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new NumericCriterion(concept, attributeFilters, timeRestriction, comparator, value, unit);
    }

    public Comparator getComparator() {
        return comparator;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Optional<String> getUnit() {
        return Optional.ofNullable(unit);
    }

    @Override
    Container<DefaultExpression> valueExpr(MappingContext mappingContext, Mapping mapping, IdentifierExpression sourceAlias) {
        if (mapping.key().termCode().equals(AgeFunctionMapping.AGE)) {
            return ageExpr();
        }

        if (mapping.valueMapping().isEmpty()) {
            throw new IllegalStateException("Numeric criterion is used with the mapping of `%s` without value mapping.".formatted(mapping.key()));
        }

        var valueMapping = mapping.valueMapping().get();

        // Mapping ensures that the termCodeMapping has exactly one type
        var castExpr = TypeExpression.of(InvocationExpression.of(sourceAlias, valueMapping.path()),
                valueMapping.types().get(0).fhirTypeName());

        return Container.of(
                ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit)));
    }

    private Container<DefaultExpression> ageExpr() {
        var ageFunc = AgeFunctionMapping.getAgeFunction(unit);
        var quantity = QuantityExpression.of(value);
        var comparatorExpr = ComparatorExpression.of(ageFunc, comparator, quantity);
        return Container.of(comparatorExpr);
    }

    private DefaultExpression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
