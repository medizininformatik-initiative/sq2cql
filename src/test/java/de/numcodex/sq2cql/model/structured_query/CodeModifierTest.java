package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeModifierTest {

    @Test
    void expression_OneCode() {
        var modifier = CodeModifier.of("status", "final");

        var expression = modifier.expression(MappingContext.of(), AliasExpression.of("O"));

        assertEquals("O.status = 'final'", PrintContext.ZERO.print(expression));
    }

    @Test
    void expression_TwoCodes() {
        var modifier = CodeModifier.of("status", "completed", "in-progress");

        var expression = modifier.expression(MappingContext.of(), AliasExpression.of("P"));

        assertEquals("P.status in { 'completed', 'in-progress' }", PrintContext.ZERO.print(expression));
    }
}
