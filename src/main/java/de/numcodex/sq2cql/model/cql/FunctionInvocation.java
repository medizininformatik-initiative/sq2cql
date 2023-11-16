package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record FunctionInvocation(String identifier, List<DefaultExpression> paramList)
        implements InvocationTerm<FunctionInvocation> {

    public FunctionInvocation {
        requireNonNull(identifier);
        paramList = List.copyOf(paramList);
    }

    public static FunctionInvocation of(String identifier, List<DefaultExpression> paramList) {
        return new FunctionInvocation(identifier, paramList);
    }

    @Override
    public String print(PrintContext printContext) {
        printContext.resetPrecedence();
        return "%s(%s)".formatted(identifier, paramList.stream().map(printContext::print)
                .collect(Collectors.joining(", ")));
    }

    @Override
    public FunctionInvocation withIncrementedSuffixes(Map<String, Integer> increments) {
        return new FunctionInvocation(identifier, paramList.stream().map(e -> e.withIncrementedSuffixes(increments)).toList());
    }
}
