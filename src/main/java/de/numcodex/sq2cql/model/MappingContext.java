package de.numcodex.sq2cql.model;

import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.structured_query.Concept;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
public record MappingContext(Map<TermCode, Mapping> mappings,
                             TermCodeNode conceptTree,
                             Map<String, String> codeSystemAliases) {

    public MappingContext {
        mappings = Map.copyOf(mappings);
        codeSystemAliases = Map.copyOf(codeSystemAliases);
    }

    public static MappingContext of() {
        return new MappingContext(Map.of(), null, Map.of());
    }

    public static MappingContext of(Map<TermCode, Mapping> mappings, TermCodeNode conceptTree,
                                    Map<String, String> codeSystemAliases) {
        return new MappingContext(mappings, conceptTree, codeSystemAliases);
    }

    public Optional<Mapping> getMapping(TermCode concept) {
        return Optional.ofNullable(mappings.get(requireNonNull(concept)));
    }

    public Stream<TermCode> expandConcept(Concept concept) {
        var expandedCodes = conceptTree == null ? List.<TermCode>of() : expandCodes(concept);
        return (expandedCodes.isEmpty() ? concept.termCodes() : expandedCodes).stream().filter(mappings::containsKey);
    }

    private List<TermCode> expandCodes(Concept concept) {
        return concept.termCodes().stream().flatMap(conceptTree::expand).toList();
    }

    public CodeSystemDefinition codeSystemDefinition(String system) {
        String name = codeSystemAliases.get(requireNonNull(system));
        if (name == null) {
            throw new IllegalStateException("code system alias for `%s` not found".formatted(system));
        }
        return CodeSystemDefinition.of(name, system);
    }
}
