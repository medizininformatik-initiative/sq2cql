package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ComparatorExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.TypeExpression;

import java.math.BigDecimal;
import java.util.Objects;

public class NumericModifier extends AbstractModifier {

    private final Comparator comparator;
    private final BigDecimal value;
    private final String unit;

    private NumericModifier(String path, Comparator comparator, BigDecimal value, String unit) {
        super(path);
        this.comparator = comparator;
        this.value = value;
        this.unit = unit;
    }

    public static NumericModifier of(String path, Comparator comparator, BigDecimal value, String unit) {
        return new NumericModifier(path, comparator, value, unit);
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression identifier) {
        var castExpr = TypeExpression.of(InvocationExpression.of(identifier, path), "Quantity");
        return Container.of(ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit)));
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericModifier that = (NumericModifier) o;
        return path.equals(that.path) && comparator.equals(that.comparator)
                && value.equals(that.value) && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, comparator, value, unit);
    }
}
