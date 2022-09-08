package de.numcodex.sq2cql.model.cql;

import static java.util.stream.Collectors.joining;

import de.numcodex.sq2cql.PrintContext;
import java.util.List;

public record Context(String contextName, List<ExpressionDefinition> expressionDefinitions) {


  public Context {
    expressionDefinitions = List.copyOf(expressionDefinitions);
  }

  public static Context of() {
    return new Context("", List.of());
  }

  public static Context of(String contextName, List<ExpressionDefinition> expressionDefinitions) {
    return new Context(contextName, expressionDefinitions);
  }

  public String print(PrintContext printContext) {
    return """
        context %s
        
        %s""".formatted(contextName, expressionDefinitions.stream().map(d -> d.print(printContext)).collect(joining("\n\n")));
  }

}
