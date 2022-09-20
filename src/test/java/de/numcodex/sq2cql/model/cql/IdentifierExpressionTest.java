package de.numcodex.sq2cql.model.cql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

public class IdentifierExpressionTest {

  @Test
  public void print_StringWithUnsupportedChars() {
    var expr = IdentifierExpression.of("Test Expression-123");
    assertEquals("\"Test Expression-123\"", expr.print(PrintContext.ZERO));
  }

  @Test
  public void print_StringWithSupportedChars() {
    var expr = IdentifierExpression.of("Test_Expression123");
    assertEquals("Test_Expression123", expr.print(PrintContext.ZERO));
  }

}
