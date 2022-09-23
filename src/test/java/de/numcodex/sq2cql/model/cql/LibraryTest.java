package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.numcodex.sq2cql.model.cql.BooleanExpression.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class LibraryTest {

    @Test
    void print_Empty() {
        var cql = Library.of().print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                                
                                
                """, cql);
    }

    @Test
    void print_OnExpressionDefinition() {
        var cql = Library.of(Set.of(), List.of(Context.of("Patient", List.of(ExpressionDefinition.of("InInitialPopulation", TRUE)))))
                .print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                
                context Patient
                                
                define InInitialPopulation:
                  true
                """, cql);
    }
}
