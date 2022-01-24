package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Expression1 OR Expression2 OR Expression ... OR ExpressionN
 */
public record OrExpression(List<BooleanExpression> expressions) implements BooleanExpression {

    public static final int PRECEDENCE = 3;

    public OrExpression {
        expressions = List.copyOf(expressions);
    }

    public static OrExpression of(BooleanExpression e1, BooleanExpression e2) {
        if (e1 instanceof OrExpression) {
            return new OrExpression(Stream.concat(((OrExpression) e1).expressions.stream(),
                    Stream.of(requireNonNull(e2))).toList());
        } else {
            return new OrExpression(List.of(requireNonNull(e1), requireNonNull(e2)));
        }
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, expressions.stream()
                .map(e -> e.print(printContext.withPrecedence(PRECEDENCE)))
                .collect(joining(" or\n" + printContext.getIndent())));
    }
}
