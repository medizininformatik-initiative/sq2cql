package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Expression1 OR Expression2 OR Expression ... OR ExpressionN
 */
public final class OrExpression implements BooleanExpression {

    private final List<BooleanExpression> expressions;

    private OrExpression(List<BooleanExpression> expressions) {
        this.expressions = expressions;
    }

    public static OrExpression of() {
        return new OrExpression(List.of());
    }

    public static OrExpression of(BooleanExpression e1, BooleanExpression e2) {
        if (e1 instanceof OrExpression) {
            return new OrExpression(Stream.concat(((OrExpression) e1).expressions.stream(),
                    Stream.of(Objects.requireNonNull(e2))).collect(Collectors.toUnmodifiableList()));
        } else {
            return new OrExpression(List.of(Objects.requireNonNull(e1), Objects.requireNonNull(e2)));
        }
    }

    @Override
    public String print(PrintContext printContext) {
        return expressions.stream()
                .map(e -> e.print(printContext))
                .map("(%s)"::formatted)
                .collect(joining(" or\n" + printContext.getIndent()));
    }
}

