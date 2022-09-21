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
                      List<Context> contexts) {

    public Library {
        codeSystemDefinitions = Set.copyOf(codeSystemDefinitions);
        contexts = List.copyOf(contexts);
    }

    public static Library of() {
        return new Library(Set.of(), List.of());
    }

    public static Library of(Set<CodeSystemDefinition> codeSystemDefinitions,
                             List<Context> contexts) {
        return new Library(codeSystemDefinitions, contexts);
    }

    public String print() {
        return codeSystemDefinitions.isEmpty()
                ? """
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                      
                %s
                """
                .formatted(contexts.stream().map(Context::print).collect(joining("\n\n")))
                : """
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                
                %s
                                
                %s
                """
                .formatted(codeSystemDefinitions.stream()
                                .sorted(Comparator.comparing(CodeSystemDefinition::name))
                                .map(CodeSystemDefinition::print).collect(joining("\n")),
                        contexts.stream().map(Context::print).collect(joining("\n\n")));
    }
}
