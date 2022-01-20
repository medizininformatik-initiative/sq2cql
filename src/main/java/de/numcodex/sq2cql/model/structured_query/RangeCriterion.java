package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import de.numcodex.sq2cql.model.cql.BetweenExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ExistsExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.cql.SourceClause;
import de.numcodex.sq2cql.model.cql.TypeExpression;
import de.numcodex.sq2cql.model.cql.WhereClause;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    private RangeCriterion(Concept concept, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        super(concept, List.of());
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);
        this.unit = unit;
    }

    public static RangeCriterion of(Concept concept, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeCriterion(concept, lowerBound, upperBound, null);
    }

    public static RangeCriterion of(Concept concept, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeCriterion(concept, lowerBound, upperBound, Objects.requireNonNull(unit));
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
        return retrieveExpr(mappingContext, termCode).map(retrieveExpr -> {
            var alias = AliasExpression.of(retrieveExpr.getResourceType().substring(0, 1));
            var sourceClause = SourceClause.of(retrieveExpr, alias);
            var mapping = mappingContext.getMapping(termCode).orElseThrow(() -> new MappingNotFoundException(termCode));
            var valueFhirPath = mapping.getValueFhirPath().orElseThrow(() -> new ValueFhirPathNotFoundException(termCode));
            var castExpr = TypeExpression.of(InvocationExpression.of(alias, valueFhirPath), "Quantity");
            var whereExpression = BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
                    quantityExpression(upperBound, unit));
            var queryExpr = QueryExpression.of(sourceClause, WhereClause.of(whereExpression));
            return ExistsExpression.of(queryExpr);
        });
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
