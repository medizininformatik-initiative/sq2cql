package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BetweenExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.TypeExpression;

import java.math.BigDecimal;

public class RangeModifier extends AbstractModifier {

    private final BigDecimal lowerBound;
    private final BigDecimal upperBound;
    private final String unit;

    public RangeModifier(String path, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        super(path);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.unit = unit;
    }

    public static RangeModifier of(String path, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeModifier(path, lowerBound, upperBound, unit);
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression identifier) {
        var castExpr = TypeExpression.of(InvocationExpression.of(identifier, path), "Quantity");
        return Container.of(BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
                quantityExpression(upperBound, unit)));
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
