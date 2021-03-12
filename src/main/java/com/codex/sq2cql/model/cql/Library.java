package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author Alexander Kiel
 */
public final class Library {

    private final Set<CodeSystemDefinition> codeSystemDefinitions;
    private final List<ExpressionDefinition> expressionDefinitions;

    private Library(Set<CodeSystemDefinition> codeSystemDefinitions,
                    List<ExpressionDefinition> expressionDefinitions) {
        this.codeSystemDefinitions = codeSystemDefinitions;
        this.expressionDefinitions = expressionDefinitions;
    }

    public static Library of(Collection<CodeSystemDefinition> codeSystemDefinitions,
                             Collection<ExpressionDefinition> expressionDefinitions) {
        return new Library(Set.copyOf(codeSystemDefinitions), List.copyOf(expressionDefinitions));
    }

    public List<ExpressionDefinition> getExpressionDefinitions() {
        return expressionDefinitions;
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
                                .sorted(Comparator.comparing(CodeSystemDefinition::getName))
                                .map(CodeSystemDefinition::print).collect(joining("\n")),
                        expressionDefinitions.stream().map(d -> d.print(printContext)).collect(joining("\n\n")));
    }
}
