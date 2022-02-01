package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class QuantityExpressionTest {

    @Test
    void print_WithoutUnit() {
        assertEquals("1", QuantityExpression.of(BigDecimal.ONE).print(PrintContext.ZERO));
    }
}
