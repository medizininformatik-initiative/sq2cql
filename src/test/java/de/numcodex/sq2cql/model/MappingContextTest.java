package de.numcodex.sq2cql.model;

import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MappingContextTest {

    public static final TermCode C1 = TermCode.of("foo", "c1", "c1-d");

    @Test
    void expandConcept_EmptyContext() {
        assertTrue(MappingContext.of().expandConcept(Concept.of(C1)).toList().isEmpty());
    }

    @Test
    void expandConcept_EmptyTree() {
        var mappings = Map.of(C1, Mapping.of(C1, "Observation"));
        var context = MappingContext.of(mappings, TermCodeNode.of(), Map.of());

        assertEquals(List.of(C1), context.expandConcept(Concept.of(C1)).toList());
    }

    @Test
    void expandConcept_MissingMapping() {
        var context = MappingContext.of(Map.of(), TermCodeNode.of(C1), Map.of());

        assertTrue(context.expandConcept(Concept.of(C1)).toList().isEmpty());
    }
}
