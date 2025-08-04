package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.*;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record NumericModifier(String path, Comparator comparator, BigDecimal value, String unit)
        implements SimpleModifier {

    public NumericModifier {
        requireNonNull(path);
        requireNonNull(comparator);
        requireNonNull(value);
        requireNonNull(unit);
    }

    public static NumericModifier of(String path, Comparator comparator, BigDecimal value, String unit) {
        return new NumericModifier(path, comparator, value, unit);
    }

    @Override
    public Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var castExpr = TypeExpression.of(InvocationExpression.of(sourceAlias, path), "Quantity");
        return Container.of(ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit)));
    }

    private DefaultExpression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }
}
