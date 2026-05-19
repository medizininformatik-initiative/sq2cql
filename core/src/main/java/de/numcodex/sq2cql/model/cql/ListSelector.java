package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public record ListSelector(List<? extends DefaultExpression> items) implements ExpressionTerm<ListSelector> {

    public ListSelector {
        items = List.copyOf(items);
    }

    public static ListSelector of(List<? extends DefaultExpression> items) {
        return new ListSelector(items);
    }

    @Override
    public String print(PrintContext printContext) {
        return "{ %s }".formatted(items.stream().map(printContext::print).collect(joining(", ")));
    }

    @Override
    public ListSelector withIncrementedSuffixes(Map<String, Integer> increments) {
        return new ListSelector(items.stream().map(e -> e.withIncrementedSuffixes(increments)).toList());
    }
}
