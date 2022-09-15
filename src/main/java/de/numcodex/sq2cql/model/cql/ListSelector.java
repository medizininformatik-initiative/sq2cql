package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;

import static java.util.stream.Collectors.joining;

public record ListSelector(List<? extends Expression> items) implements ExpressionTerm {

    public ListSelector {
        items = List.copyOf(items);
    }

    public static ListSelector of(List<? extends Expression> items) {
        return new ListSelector(items);
    }

    @Override
    public String print(PrintContext printContext) {
        return "{ %s }".formatted(items.stream().map(printContext::print).collect(joining(", ")));
    }
}
