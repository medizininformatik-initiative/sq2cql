package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public record AdditionExpressionTerm(List<ExpressionTerm> expressions) implements
        ExpressionTerm {

    public static final int PRECEDENCE = 16;


    public AdditionExpressionTerm {
        expressions = List.copyOf(expressions);
    }


    public static Expression of(ExpressionTerm e1, ExpressionTerm e2) {
        if (e1 instanceof AdditionExpressionTerm) {
            return new AdditionExpressionTerm(
                    Stream.concat(((AdditionExpressionTerm) e1).expressions.stream(),
                            Stream.of(requireNonNull(e2))).toList());
        } else {
            return new AdditionExpressionTerm(List.of(requireNonNull(e1), requireNonNull(e2)));
        }
    }


    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, expressions.stream()
                .map(printContext.withPrecedence(PRECEDENCE)::print)
                .collect(joining(" + ")));
    }
}
