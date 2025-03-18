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

        //noinspection OptionalGetWithoutIsPresent
        return Container.of(mapping.types().stream().sorted().map(type -> switch (type) {
            case DATE -> dateExpr(invocationExpr, IntervalSelector.of(DateExpression.of(afterDate), DateExpression.of(beforeDate)));
            case DATE_TIME -> dateTimeExpr(invocationExpr, IntervalSelector.of(DateTimeExpression.of(afterDate), DateTimeExpression.of(beforeDate)));
            case PERIOD -> OverlapsIntervalOperatorPhrase.of(invocationExpr, IntervalSelector.of(DateTimeExpression.of(afterDate), DateTimeExpression.of(beforeDate)));
        }).reduce(OrExpression::of).get());
    }
}
