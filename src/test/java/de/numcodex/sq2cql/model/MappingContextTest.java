package de.numcodex.sq2cql.model;

import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.ContextualConcept;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static de.numcodex.sq2cql.Util.createTreeWithoutChildren;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingContextTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode C1 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c1", "c1-d"));
    static final String CODE_SYSTEM_URL = "url-164919";
    static final String ALIAS = "alias-164923";

    @Test
    void expandConcept_EmptyContext() {
        var context = MappingContext.of();

        var termCodes = context.expandConcept(ContextualConcept.of(C1)).toList();

        assertTrue(termCodes.isEmpty());
    }

    @Test
    void expandConcept_EmptyTree() {
        var context = MappingContext.of(Map.of(C1, Mapping.of(C1, "Observation")), null, Map.of());

        var termCodes = context.expandConcept(ContextualConcept.of(C1)).toList();

        assertEquals(List.of(C1), termCodes);
    }

    @Test
    void expandConcept_MissingMapping() {
        var context = MappingContext.of(Map.of(), createTreeWithoutChildren(C1), Map.of());

        var termCodes = context.expandConcept(ContextualConcept.of(C1)).toList();

        assertTrue(termCodes.isEmpty());
    }

    @Test
    void codeSystemDefinition_ExistingAlias() {
        var context = MappingContext.of(Map.of(), null, Map.of(CODE_SYSTEM_URL, ALIAS));

        var definition = context.findCodeSystemDefinition(CODE_SYSTEM_URL);

        assertTrue(definition.isPresent());
        assertEquals(ALIAS, definition.get().name());
        assertEquals(CODE_SYSTEM_URL, definition.get().system());
    }

    @Test
    void codeSystemDefinition_MissingAlias() {
        var context = MappingContext.of(Map.of(), null, Map.of(CODE_SYSTEM_URL, ALIAS));

        var definition = context.findCodeSystemDefinition("missing-url-165330");

        assertTrue(definition.isEmpty());
    }
}
