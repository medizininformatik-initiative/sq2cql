package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

public record TimeRestrictionModifier(String path, String afterDate, String beforeDate) implements SimpleModifier {

    public TimeRestrictionModifier {
        requireNonNull(path);
        afterDate = afterDate == null ? "1900-01-01T" : afterDate;
        beforeDate = beforeDate == null ? "2040-01-01T" : beforeDate;
    }

    public static TimeRestrictionModifier of(String path, String afterDate, String beforeDate) {
        return new TimeRestrictionModifier(path, afterDate, beforeDate);
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var invocationExpr = InvocationExpression.of(sourceAlias, path);
        var castExp = TypeExpression.of(invocationExpr, "dateTime");
        var toDateFunction = FunctionInvocation.of("ToDate", List.of(castExp));
        var intervalSelector = IntervalSelector.of(DateTimeExpression.of(afterDate), DateTimeExpression.of(beforeDate));
        var dateTimeInExpr = MembershipExpression.in(toDateFunction, intervalSelector);
        var intervalOverlapExpr = OverlapsIntervalOperatorPhrase.of(invocationExpr, intervalSelector);
        return Container.of(OrExpression.of(dateTimeInExpr, intervalOverlapExpr));
    }
}
