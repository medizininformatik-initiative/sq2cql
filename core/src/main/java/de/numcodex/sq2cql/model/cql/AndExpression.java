package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Expression1 AND Expression2 AND Expression ... AND ExpressionN
 */
public record AndExpression(List<DefaultExpression> expressions) implements DefaultExpression {

    public static final int PRECEDENCE = 4;

    public AndExpression {
        expressions = List.copyOf(expressions);
    }

    public static DefaultExpression of(DefaultExpression e1, DefaultExpression e2) {
        if (e1 == Expression.TRUE) {
            return e2;
        } else if (e2 == Expression.TRUE) {
            return e1;
        } else if (e1 instanceof AndExpression) {
            return new AndExpression(Stream.concat(((AndExpression) e1).expressions.stream(),
                    Stream.of(new WrapperExpression(e2))).toList());
        } else {
            return new AndExpression(List.of(new WrapperExpression(e1), new WrapperExpression(e2)));
        }
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, expressions.stream()
                .map(printContext.withPrecedence(PRECEDENCE)::print)
                .collect(joining(" and\n" + printContext.getIndent())));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new AndExpression(expressions.stream().map(e -> e.withIncrementedSuffixes(increments)).toList());
    }
}
