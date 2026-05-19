package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record MembershipExpression(Expression<?> a, String op, Expression<?> b) implements DefaultExpression {

    public static final int PRECEDENCE = 5;

    public MembershipExpression {
        requireNonNull(a);
        requireNonNull(b);
    }

    public static DefaultExpression contains(Expression<?> a, Expression<?> b) {
        return new MembershipExpression(a, "contains", b);
    }

    public static DefaultExpression in(Expression<?> a, Expression<?> b) {
        return new MembershipExpression(a, "in", b);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s %s %s".formatted(a.print(childPrintContext), op,
                b.print(childPrintContext)));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new MembershipExpression(a.withIncrementedSuffixes(increments), op,
                b.withIncrementedSuffixes(increments));
    }
}
