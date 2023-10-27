package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record RangeModifier(String path, BigDecimal lowerBound, BigDecimal upperBound, String unit)
        implements SimpleModifier {

    public RangeModifier {
        requireNonNull(path);
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
        requireNonNull(unit);
    }

    public static RangeModifier of(String path, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeModifier(path, lowerBound, upperBound, unit);
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var castExpr = TypeExpression.of(InvocationExpression.of(sourceAlias, path), "Quantity");
        return Container.of(BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
                quantityExpression(upperBound, unit)));
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
