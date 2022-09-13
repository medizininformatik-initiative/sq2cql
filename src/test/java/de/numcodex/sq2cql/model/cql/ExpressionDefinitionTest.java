package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionDefinitionTest {

    @Test
    void print_query() {
        var expr = ExpressionDefinition.of("foo", QueryExpressionTest.query());

        var s = expr.print(PrintContext.ZERO);

        assertEquals("""
                define foo:
                  from [Observation: Code '85354-9' from loinc] O
                    where true""", s);
    }
}
