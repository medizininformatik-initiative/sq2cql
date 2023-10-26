package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifierExpressionTest {

    @Nested
    class Print {

        @Test
        public void withSpace() {
            var expr = IdentifierExpression.of("Test Expression");

            assertEquals("\"Test Expression\"", expr.print(PrintContext.ZERO));
        }

        @Test
        public void withDash() {
            var expr = IdentifierExpression.of("test-expression");

            assertEquals("\"test-expression\"", expr.print(PrintContext.ZERO));
        }

        @Test
        public void withSupportedChars() {
            var expr = IdentifierExpression.of("Test_Expression123");

            assertEquals("Test_Expression123", expr.print(PrintContext.ZERO));
        }
    }
}
