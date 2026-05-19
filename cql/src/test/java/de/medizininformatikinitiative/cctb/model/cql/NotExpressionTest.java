package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;
import org.junit.jupiter.api.Test;

import static de.medizininformatikinitiative.cctb.model.cql.Expression.TRUE;
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
