package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Expression1 AND Expression2 AND Expression ... AND ExpressionN
 */
public final class AndExpression implements BooleanExpression {

    private final List<BooleanExpression> expressions;

    private AndExpression(List<BooleanExpression> expressions) {
        this.expressions = expressions;
    }

    public static AndExpression of() {
        return new AndExpression(List.of());
    }

    public static AndExpression of(BooleanExpression e1, BooleanExpression e2) {
        if (e1 instanceof AndExpression) {
            return new AndExpression(Stream.concat(((AndExpression) e1).expressions.stream(),
                    Stream.of(Objects.requireNonNull(e2))).collect(Collectors.toUnmodifiableList()));
        } else {
            return new AndExpression(List.of(Objects.requireNonNull(e1), Objects.requireNonNull(e2)));
        }
    }

    @Override
    public String print(PrintContext printContext) {
        return expressions.stream()
                .map(e -> e.print(printContext))
                .map("(%s)"::formatted)
                .collect(joining(" and\n" + printContext.getIndent()));
    }
}

