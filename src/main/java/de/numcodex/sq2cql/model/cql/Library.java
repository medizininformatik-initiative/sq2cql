package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author Alexander Kiel
 */
public record Library(Set<CodeSystemDefinition> codeSystemDefinitions,
                      List<ExpressionDefinition> expressionDefinitions) {

    public Library {
        codeSystemDefinitions = Set.copyOf(codeSystemDefinitions);
        expressionDefinitions = List.copyOf(expressionDefinitions);
    }

    public static Library of() {
        return new Library(Set.of(), List.of());
    }

    public static Library of(Set<CodeSystemDefinition> codeSystemDefinitions,
                             List<ExpressionDefinition> expressionDefinitions) {
        return new Library(codeSystemDefinitions, expressionDefinitions);
    }

    public String print(PrintContext printContext) {
        return codeSystemDefinitions.isEmpty()
                ? """
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                
                %s
                """
                .formatted(expressionDefinitions.stream().map(d -> d.print(printContext)).collect(joining("\n\n")))
                : """
                library Retrieve
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                
                %s
                                
                %s
                """
                .formatted(codeSystemDefinitions.stream()
                                .sorted(Comparator.comparing(CodeSystemDefinition::name))
                                .map(CodeSystemDefinition::print).collect(joining("\n")),
                        expressionDefinitions.stream().map(d -> d.print(printContext)).collect(joining("\n\n")));
    }
}
