package de.numcodex.sq2cql.model;

import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;

import de.numcodex.sq2cql.model.structured_query.ContextualConcept;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A context holding information to facilitate the mapping process.
 *
 * @author Alexander Kiel
 */
public class MappingContext {

    private final Map<ContextualTermCode, Mapping> mappings;
    private final TermCodeNode conceptTree;
    private final Map<String, CodeSystemDefinition> codeSystemDefinitions;

    private MappingContext(Map<ContextualTermCode, Mapping> mappings, TermCodeNode conceptTree,
                           Map<String, CodeSystemDefinition> codeSystemDefinitions) {
        this.mappings = mappings;
        this.conceptTree = conceptTree;
        this.codeSystemDefinitions = codeSystemDefinitions;
    }

    /**
     * Returns an empty mapping context.
     *
     * @return the mapping context
     */
    public static MappingContext of() {
        return new MappingContext(Map.of(), null, Map.of());
    }

    /**
     * Returns a mapping context.
     *
     * @param mappings          the mappings keyed by their term code
     * @param conceptTree       a tree of concepts to expand (can be null)
     * @param codeSystemAliases a map of code system URLs to their aliases
     * @return the mapping context
     */
    public static MappingContext of(Map<ContextualTermCode, Mapping> mappings, TermCodeNode conceptTree,
                                    Map<String, String> codeSystemAliases) {
        return new MappingContext(Map.copyOf(mappings), conceptTree, codeSystemAliases.entrySet().stream()
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey,
                        e -> CodeSystemDefinition.of(e.getValue(), e.getKey()))));
    }

    /**
     * Tries to find the {@link Mapping} with the given {@code key}.
     *
     * @param key the TermCode of the mapping
     * @return either the Mapping or {@code Optional#empty() nothing}
     */
    public Optional<Mapping> findMapping(ContextualTermCode key) {
        return Optional.ofNullable(mappings.get(requireNonNull(key)));
    }

    /**
     * Expands {@code concept} into a stream of {@link TermCode TermCodes}.
     *
     * @param concept the concept to expand
     * @return the stream of TermCodes
     */
    public Stream<ContextualTermCode> expandConcept(ContextualConcept concept) {
        List<ContextualTermCode> expandedCodes = conceptTree == null ? List.of() : expandCodes(concept);
        List<ContextualTermCode> concepts = expandedCodes.isEmpty() ? concept.contextualTermCodes() : expandedCodes;
        return concepts.stream().filter(mappings::containsKey);
    }

    private List<ContextualTermCode> expandCodes(ContextualConcept concept) {
        return concept.contextualTermCodes().stream().flatMap(conceptTree::expand).toList();
    }

    /**
     * Tries to find the {@link CodeSystemDefinition} with the given {@code system} URL.
     *
     * @param system the URL of the code system
     * @return either the CodeSystemDefinition or {@code Optional#empty() nothing}
     */
    public Optional<CodeSystemDefinition> findCodeSystemDefinition(String system) {
        return Optional.ofNullable(codeSystemDefinitions.get(requireNonNull(system)));
    }
}
