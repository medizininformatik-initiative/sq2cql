package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public record ListSelector(List<Expression> items) implements TermExpression {

    public ListSelector {
        items = List.copyOf(items);
    }

    public static ListSelector of(List<Expression> items) {
        return new ListSelector(items);
    }

    @Override
    public String print(PrintContext printContext) {
        return "{ %s }".formatted(items.stream().map(e -> e.print(printContext))
                .collect(joining(", ")));
    }
}
