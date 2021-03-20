package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class MembershipExpression implements BooleanExpression {

    public static final int PRECEDENCE = 5;

    private final Expression a;
    private final String op;
    private final Expression b;

    private MembershipExpression(Expression a, String op, Expression b) {
        this.a = Objects.requireNonNull(a);
        this.op = op;
        this.b = Objects.requireNonNull(b);
    }

    public static MembershipExpression contains(Expression a, Expression b) {
        return new MembershipExpression(a, "contains", b);
    }

    public static MembershipExpression in(Expression a, Expression b) {
        return new MembershipExpression(a, "in", b);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s %s %s".formatted(a.print(childPrintContext), op,
                b.print(childPrintContext)));
    }
}
