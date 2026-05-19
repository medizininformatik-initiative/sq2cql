package de.medizininformatikinitiative.cctb.model.cql;

import de.medizininformatikinitiative.cctb.PrintContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class QuantityExpressionTest {

    @Nested
    class Print {

        @Test
        void withoutUnit() {
            assertEquals("1", QuantityExpression.of(BigDecimal.ONE).print(PrintContext.ZERO));
        }

        @Test
        void withUnit() {
            assertEquals("1 'kg'", QuantityExpression.of(BigDecimal.ONE, "kg").print(PrintContext.ZERO));
        }

        @Test
        void withUnitToEscape() {
            assertEquals("1 '[arb\\'U]/mL'", QuantityExpression.of(BigDecimal.ONE, "[arb'U]/mL").print(PrintContext.ZERO));
            assertEquals("1 '\\'\\''", QuantityExpression.of(BigDecimal.ONE, "''").print(PrintContext.ZERO));
        }
    }
}
