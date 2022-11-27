package de.numcodex.sq2cql.model.cql;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.PrintContext;
import java.util.List;
import java.util.stream.Collectors;

public record FunctionInvocation(String identifier, List<Expression> paramList) implements InvocationTerm {

  public FunctionInvocation {
    requireNonNull(identifier);
    paramList = List.copyOf(paramList);
  }

  public static FunctionInvocation of(String identifier, List<Expression> paramList) {
    return new FunctionInvocation(identifier, paramList);
  }

  @Override
  public String print(PrintContext printContext) {
    printContext.resetPrecedence();
    return "%s(%s)".formatted(identifier, paramList.stream().map(printContext::print)
        .collect(Collectors.joining(", ")));
  }
}
