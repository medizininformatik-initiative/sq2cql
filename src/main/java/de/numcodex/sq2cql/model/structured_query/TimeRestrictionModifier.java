package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record TimeRestrictionModifier(String path, LocalDate afterDate, LocalDate beforeDate) implements SimpleModifier {

    public TimeRestrictionModifier {
        requireNonNull(path);
        requireNonNull(afterDate);
        requireNonNull(beforeDate);
    }

    public static TimeRestrictionModifier of(String path, LocalDate afterDate, LocalDate beforeDate) {
        return new TimeRestrictionModifier(path, afterDate, beforeDate);
    }

    @Override
    public Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var invocationExpr = InvocationExpression.of(sourceAlias, path);
        var castExp = TypeExpression.of(invocationExpr, "dateTime");
        var toDateFunction = FunctionInvocation.of("ToDate", List.of(castExp));
        var intervalSelector = IntervalSelector.of(DateTimeExpression.of(afterDate.toString()), DateTimeExpression.of(beforeDate.toString()));
        var dateTimeInExpr = MembershipExpression.in(toDateFunction, intervalSelector);

        if ("recordedDate".equals(path)) {
            return Container.of(dateTimeInExpr);
        }

        var intervalOverlapExpr = OverlapsIntervalOperatorPhrase.of(invocationExpr, intervalSelector);
        return Container.of(OrExpression.of(dateTimeInExpr, intervalOverlapExpr));
    }
}
