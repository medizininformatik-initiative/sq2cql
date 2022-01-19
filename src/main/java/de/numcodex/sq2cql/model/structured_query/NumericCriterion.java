package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ComparatorExpression;
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
 * A {@code NumericCriterion} will select all patients that have at least one resource represented by that concept and
 * numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory value.
 */
public final class NumericCriterion extends AbstractCriterion {

    private final Comparator comparator;
    private final BigDecimal value;
    private final String unit;

    private NumericCriterion(Concept concept, Comparator comparator, BigDecimal value, String unit) {
        super(concept, List.of());
        this.value = Objects.requireNonNull(value);
        this.comparator = Objects.requireNonNull(comparator);
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
    public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value) {
        return new NumericCriterion(concept, comparator, value, null);
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
    public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value, String unit) {
        return new NumericCriterion(concept, comparator, value, Objects.requireNonNull(unit));
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
            var whereExpression = ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit));
            var queryExpr = QueryExpression.of(sourceClause, WhereClause.of(whereExpression));
            return ExistsExpression.of(queryExpr);
        });
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
