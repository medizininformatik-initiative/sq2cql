package com.codex.sq2cql.model.structured_query;

import com.codex.sq2cql.Container;
import com.codex.sq2cql.model.MappingContext;
import com.codex.sq2cql.model.common.TermCode;
import com.codex.sq2cql.model.cql.AliasExpression;
import com.codex.sq2cql.model.cql.BetweenExpression;
import com.codex.sq2cql.model.cql.BooleanExpression;
import com.codex.sq2cql.model.cql.ExistsExpression;
import com.codex.sq2cql.model.cql.InvocationExpression;
import com.codex.sq2cql.model.cql.QuantityExpression;
import com.codex.sq2cql.model.cql.QueryExpression;
import com.codex.sq2cql.model.cql.SourceClause;
import com.codex.sq2cql.model.cql.TypeExpression;
import com.codex.sq2cql.model.cql.WhereClause;

import java.math.BigDecimal;
import java.util.Objects;

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

    private RangeCriterion(TermCode concept, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        super(concept);
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);
        this.unit = unit;
    }

    public static RangeCriterion of(TermCode concept, BigDecimal minValue, BigDecimal maxValue, String unit) {
        return new RangeCriterion(concept, minValue, maxValue, unit);
    }

    @Override
    public Container<BooleanExpression> toCql(MappingContext mappingContext) {
        return retrieveExpr(mappingContext).map(retrieveExpr -> {
            var alias = AliasExpression.of(retrieveExpr.getResourceType().substring(0, 1));
            var sourceClause = SourceClause.of(retrieveExpr, alias);
            var castExpr = TypeExpression.of(InvocationExpression.of(alias, "value"), "Quantity");
            var whereExpression = BetweenExpression.of(castExpr, QuantityExpression.of(lowerBound, unit),
                    QuantityExpression.of(upperBound, unit));
            var queryExpr = QueryExpression.of(sourceClause, WhereClause.of(whereExpression));
            return ExistsExpression.of(queryExpr);
        });
    }
}
