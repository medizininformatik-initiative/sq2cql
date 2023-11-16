package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import static de.numcodex.sq2cql.model.cql.Expression.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class NotExpressionTest {

    @Test
    void print_HigherPrecedenceChild() {
        var cql = NotExpression.of(TRUE).print(PrintContext.ZERO);

        assertEquals("not true", cql);
    }

    @Test
    void print_LowerPrecedenceChild() {
        var a = new WrapperExpression(StandardIdentifierExpression.of("a"));
        var b = new WrapperExpression(StandardIdentifierExpression.of("b"));
        var expr = NotExpression.of(AndExpression.of(a, b));

        var cql = expr.print(PrintContext.ZERO);

        assertEquals("not (a and\nb)", cql);
    }
}
