package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Expression1 UNION Expression2 UNION Expression ... UNION ExpressionN
 */
public record UnionExpression(List<DefaultExpression> expressions) implements DefaultExpression {

    public static final int PRECEDENCE = 1;

    public UnionExpression {
        expressions = List.copyOf(expressions);
    }

    public static <T extends Expression<T>, U extends Expression<U>> UnionExpression of(Expression<T> e1, Expression<U> e2) {
        if (e1 instanceof UnionExpression) {
            return new UnionExpression(Stream.concat(((UnionExpression) e1).expressions.stream(),
                    Stream.of(new WrapperExpression(e2))).toList());
        } else {
            return new UnionExpression(List.of(new WrapperExpression(e1), new WrapperExpression(e2)));
        }
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new UnionExpression(expressions.stream().map(e -> e.withIncrementedSuffixes(increments)).toList());
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, expressions.stream()
                .map(printContext.withPrecedence(PRECEDENCE)::print)
                .collect(joining(" union\n" + printContext.getIndent())));
    }
}
