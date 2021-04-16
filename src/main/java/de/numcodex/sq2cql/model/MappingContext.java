package de.numcodex.sq2cql.model;

import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Alexander Kiel
 */
public final class MappingContext {

    private final Map<TermCode, Mapping> mappings;
    private final ConceptNode conceptTree;
    private final Map<String, String> codeSystemAliases;

    private MappingContext(Map<TermCode, Mapping> mappings, ConceptNode conceptTree,
                           Map<String, String> codeSystemAliases) {
        this.mappings = Objects.requireNonNull(mappings);
        this.conceptTree = Objects.requireNonNull(conceptTree);
        this.codeSystemAliases = Objects.requireNonNull(codeSystemAliases);
    }

    public static MappingContext of() {
        return new MappingContext(Map.of(), ConceptNode.of(), Map.of());
    }

    public static MappingContext of(Map<TermCode, Mapping> mappings, ConceptNode conceptTree,
                                    Map<String, String> codeSystemAliases) {
        return new MappingContext(Map.copyOf(mappings), conceptTree, Map.copyOf(codeSystemAliases));
    }

    public Optional<Mapping> getMapping(TermCode concept) {
        return Optional.ofNullable(mappings.get(Objects.requireNonNull(concept)));
    }

    public Stream<TermCode> expandConcept(TermCode concept) {
        return conceptTree.expand(concept);
    }

    public CodeSystemDefinition codeSystemDefinition(String system) {
        String name = codeSystemAliases.get(Objects.requireNonNull(system));
        if (name == null) {
            throw new IllegalStateException("code system alias for `%s` not found".formatted(system));
        }
        return CodeSystemDefinition.of(name, system);
    }
}
