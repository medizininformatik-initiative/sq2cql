package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@code RangeCriterion} will select all patients that have at least one resource represented by
 * that concept and a range of numeric values.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class RangeCriterion extends AbstractCriterion<RangeCriterion> {

    private final BigDecimal lowerBound;
    private final BigDecimal upperBound;
    private final String unit;

    private RangeCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
                           TimeRestriction timeRestriction, BigDecimal lowerBound,
                           BigDecimal upperBound, String unit) {
        super(concept, attributeFilters, timeRestriction);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.unit = unit;
    }

    public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeCriterion(concept, List.of(), null, requireNonNull(lowerBound),
                requireNonNull(upperBound), null);
    }

    public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound, BigDecimal upperBound,
                                    String unit) {
        return new RangeCriterion(concept, List.of(), null, requireNonNull(lowerBound),
                requireNonNull(upperBound), requireNonNull(unit));
    }

    public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound, BigDecimal upperBound,
                                    TimeRestriction timeRestriction) {
        return new RangeCriterion(concept, List.of(), timeRestriction, requireNonNull(lowerBound),
                requireNonNull(upperBound), null);
    }

    public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound, BigDecimal upperBound,
                                    String unit, TimeRestriction timeRestriction) {
        return new RangeCriterion(concept, List.of(), timeRestriction, requireNonNull(lowerBound),
                requireNonNull(upperBound), requireNonNull(unit));
    }

    @Override
    public RangeCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
        var attributeFilters = new LinkedList<>(this.attributeFilters);
        attributeFilters.add(attributeFilter);
        return new RangeCriterion(concept, attributeFilters, timeRestriction, lowerBound, upperBound, unit);
    }

    public BigDecimal getLowerBound() {
        return lowerBound;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
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

        return Container.of(BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
                quantityExpression(upperBound, unit)));
    }

    private Container<DefaultExpression> ageExpr() {
        var ageFunc = AgeFunctionMapping.getAgeFunction(unit);
        var lower = QuantityExpression.of(lowerBound);
        var upper = QuantityExpression.of(upperBound);
        var between = BetweenExpression.of(ageFunc, lower, upper);
        return Container.of(between);
    }


    private DefaultExpression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
