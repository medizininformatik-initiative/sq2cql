package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.Lists;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BetweenExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.SourceClause;
import de.numcodex.sq2cql.model.cql.TypeExpression;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@code RangeCriterion} will select all patients that have at least one resource represented by that concept and
 * a range of numeric values.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory value.
 */
public final class RangeCriterion extends AbstractCriterion {

    private final BigDecimal lowerBound;
    private final BigDecimal upperBound;
    private final String unit;

    private RangeCriterion(Concept concept, List<AttributeFilter> attributeFilters, BigDecimal lowerBound,
                           BigDecimal upperBound, String unit) {
        super(concept, attributeFilters);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.unit = unit;
    }

    public static RangeCriterion of(Concept concept, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeCriterion(concept, List.of(), requireNonNull(lowerBound), requireNonNull(upperBound), null);
    }

    public static RangeCriterion of(Concept concept, BigDecimal lowerBound, BigDecimal upperBound, String unit,
                                    AttributeFilter... attributeFilters) {
        return new RangeCriterion(concept, List.of(attributeFilters), requireNonNull(lowerBound),
                requireNonNull(upperBound), requireNonNull(unit));
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
    public Container<BooleanExpression> toCql(MappingContext mappingContext) {
        var expr = fullExpr(mappingContext);
        if (expr.isEmpty()) {
            throw new TranslationException("Failed to expand the concept %s.".formatted(concept));
        }
        return expr;
    }

    /**
     * Builds an OR-expression with an expression for each concept of the expansion of {@code termCode}.
     */
    private Container<BooleanExpression> fullExpr(MappingContext mappingContext) {
        return mappingContext.expandConcept(concept)
                .map(termCode -> expr(mappingContext, termCode))
                .reduce(Container.empty(), Container.OR);
    }

    private Container<BooleanExpression> expr(MappingContext mappingContext, TermCode termCode) {
        return retrieveExpr(mappingContext, termCode).flatMap(retrieveExpr -> {
            var alias = retrieveExpr.alias();
            var sourceClause = SourceClause.of(retrieveExpr, alias);
            var mapping = mappingContext.findMapping(termCode).orElseThrow(() -> new MappingNotFoundException(termCode));
            var castExpr = TypeExpression.of(InvocationExpression.of(alias, mapping.valueFhirPath()), "Quantity");
            var valueExpr = BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
                    quantityExpression(upperBound, unit));
            var modifiers = Lists.concat(mapping.fixedCriteria(), resolveAttributeModifiers(mapping.attributeMappings()));
            if (modifiers.isEmpty()) {
                return Container.of(existsExpr(sourceClause, valueExpr));
            } else {
                var modifiersExpr = modifiersExpr(modifiers, mappingContext, alias);
                return Container.AND.apply(Container.of(valueExpr), modifiersExpr)
                        .map(expr -> existsExpr(sourceClause, expr));
            }
        });
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
