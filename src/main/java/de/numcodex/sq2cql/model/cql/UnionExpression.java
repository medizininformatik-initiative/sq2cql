package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Expression1 UNION Expression2 UNION Expression ... UNION ExpressionN
 */
public record UnionExpression(List<Expression> expressions) implements Expression {

    public static final int PRECEDENCE = 1;

    public UnionExpression {
        expressions = List.copyOf(expressions);
    }

    public static UnionExpression of(Expression e1, Expression e2) {
        if (e1 instanceof UnionExpression) {
            return new UnionExpression(Stream.concat(((UnionExpression) e1).expressions.stream(),
                    Stream.of(requireNonNull(e2))).toList());
        } else {
            return new UnionExpression(List.of(requireNonNull(e1), requireNonNull(e2)));
        }
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, expressions.stream()
                .map(printContext.withPrecedence(PRECEDENCE)::print)
                .collect(joining(" union\n" + printContext.getIndent())));
    }
}
