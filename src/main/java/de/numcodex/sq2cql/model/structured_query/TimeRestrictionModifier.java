package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record TimeRestrictionModifier(Mapping.TimeRestrictionMapping mapping, LocalDate afterDate,
                                      LocalDate beforeDate) implements SimpleModifier {

    public TimeRestrictionModifier {
        requireNonNull(mapping);
        requireNonNull(afterDate);
        requireNonNull(beforeDate);
    }

    public static TimeRestrictionModifier of(Mapping.TimeRestrictionMapping mapping, LocalDate afterDate, LocalDate beforeDate) {
        return new TimeRestrictionModifier(mapping, afterDate, beforeDate);
    }

    private static DefaultExpression dateExpr(InvocationExpression invocationExpr, IntervalSelector intervalSelector) {
        var castExp = TypeExpression.of(invocationExpr, "date");
        var toDateFunction = FunctionInvocation.of("ToDate", List.of(castExp));
        return MembershipExpression.in(toDateFunction, intervalSelector);
    }

    private static DefaultExpression dateTimeExpr(InvocationExpression invocationExpr, IntervalSelector intervalSelector) {
        var castExp = TypeExpression.of(invocationExpr, "dateTime");
        var toDateFunction = FunctionInvocation.of("ToDate", List.of(castExp));
        return MembershipExpression.in(toDateFunction, intervalSelector);
    }

    @Override
    public Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var invocationExpr = InvocationExpression.of(sourceAlias, mapping.path());
        var intervalSelector = IntervalSelector.of(DateTimeExpression.of(afterDate.toString()), DateTimeExpression.of(beforeDate.toString()));

        //noinspection OptionalGetWithoutIsPresent
        return Container.of(mapping.types().stream().map(type -> switch (type) {
            case DATE -> dateExpr(invocationExpr, intervalSelector);
            case DATE_TIME -> dateTimeExpr(invocationExpr, intervalSelector);
            case PERIOD -> OverlapsIntervalOperatorPhrase.of(invocationExpr, intervalSelector);
        }).reduce(OrExpression::of).get());
    }
}
