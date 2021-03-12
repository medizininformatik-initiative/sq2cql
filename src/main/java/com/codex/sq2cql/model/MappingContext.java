package com.codex.sq2cql.model;

import com.codex.sq2cql.model.common.TermCode;
import com.codex.sq2cql.model.cql.CodeSystemDefinition;

import java.util.Map;
import java.util.Objects;

/**
 * @author Alexander Kiel
 */
public final class MappingContext {

    private final Map<TermCode, Mapping> mappings;
    private final Map<String, String> codeSystemAliases;

    private MappingContext(Map<TermCode, Mapping> mappings, Map<String, String> codeSystemAliases) {
        this.mappings = Objects.requireNonNull(mappings);
        this.codeSystemAliases = Objects.requireNonNull(codeSystemAliases);
    }

    public static MappingContext of() {
        return new MappingContext(Map.of(), Map.of());
    }

    public static MappingContext of(Map<TermCode, Mapping> mappings, Map<String, String> codeSystemAliases) {
        return new MappingContext(Map.copyOf(mappings), Map.copyOf(codeSystemAliases));
    }

    public Mapping getMapping(TermCode concept) {
        var mapping = mappings.get(Objects.requireNonNull(concept));
        if (mapping == null) {
            throw new RuntimeException("mapping for concept with code `%s` not found".formatted(concept.getCode()));
        }
        return mapping;
    }

    public CodeSystemDefinition codeSystemDefinition(String system) {
        String name = codeSystemAliases.get(Objects.requireNonNull(system));
        if (name == null) {
            throw new IllegalStateException("code system alias for `%s` not found".formatted(system));
        }
        return CodeSystemDefinition.of(name, system);
    }
}
