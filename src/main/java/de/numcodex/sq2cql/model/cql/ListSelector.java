package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class ListSelector implements TermExpression {

    private final List<Expression> items;

    private ListSelector(List<Expression> items) {
        this.items = items;
    }

    public static ListSelector of(List<Expression> items) {
        return new ListSelector(List.copyOf(items));
    }

    @Override
    public String print(PrintContext printContext) {
        return "{ %s }".formatted(items.stream().map(e -> e.print(printContext))
                .collect(joining(", ")));
    }
}
