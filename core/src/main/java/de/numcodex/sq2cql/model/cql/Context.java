package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * A context inside a {@link Container}.
 *
 * @param name                  the name of the context like {@literal Patient} or {@literal Unfiltered}
 * @param expressionDefinitions the list of expression definitions of the context
 */
public record Context(String name, List<ExpressionDefinition> expressionDefinitions) {

    public Context {
        requireNonNull(name);
        expressionDefinitions = List.copyOf(expressionDefinitions);
    }

    public static Context of(String contextName, List<ExpressionDefinition> expressionDefinitions) {
        return new Context(contextName, expressionDefinitions);
    }

    public String print() {
        return """
                context %s
                        
                %s
                """.formatted(name, expressionDefinitions.stream().map(d -> d.print(PrintContext.ZERO)).collect(joining("\n\n")));
    }
}
