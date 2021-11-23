package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.ListSelector;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.StringLiteralExpression;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alexander Kiel
 */
public final class TimeRestrictionModifier implements Modifier {

    private final LocalDate beforeDate;
    private final LocalDate afterDate;

    private TimeRestrictionModifier(LocalDate beforeDate, LocalDate afterDate) {
        this.beforeDate = beforeDate;
        this.afterDate = afterDate;
    }

    public Container<BooleanExpression> expression(MappingContext mappingContext, Expression alias) {
        var propertyExpr = InvocationExpression.of(alias, path);
        var list = ListSelector.of(beforeDate.stream().map(StringLiteralExpression::of).collect(Collectors.toList()));
        return Container.of(MembershipExpression.in(propertyExpr, list));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRestrictionModifier that = (TimeRestrictionModifier) o;
        return path.equals(that.path) && beforeDate.equals(that.beforeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, beforeDate);
    }
}
