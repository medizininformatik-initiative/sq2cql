package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeRestrictionModifierTest {

  @Test
  void expression_before() {
    var timeRestriction = TimeRestrictionModifier.of("effective", null, "2021-01-01T");
    var identifier = IdentifierExpression.of("O");
    var expression = timeRestriction.expression(MappingContext.of(), identifier);

    assertEquals("""
        O.effective as dateTime in Interval[@1900-01-01T, @2021-01-01T] or
        O.effective overlaps Interval[@1900-01-01T, @2021-01-01T]""", PrintContext.ZERO.print(expression));
  }


  @Test
  void expression_after() {
    var timeRestriction = TimeRestrictionModifier.of("effective", "2020-01-01T", null);
    var identifier = IdentifierExpression.of("O");
    var expression = timeRestriction.expression(MappingContext.of(), identifier);

    assertEquals("""
        O.effective as dateTime in Interval[@2020-01-01T, @2040-01-01T] or
        O.effective overlaps Interval[@2020-01-01T, @2040-01-01T]""", PrintContext.ZERO.print(expression));
  }


  @Test
  void expression_beforeAfter() {
    var timeRestriction = TimeRestrictionModifier.of("effective", "2021-01-01T", "2021-01-01T");
    var identifier = IdentifierExpression.of("O");
    var expression = timeRestriction.expression(MappingContext.of(), identifier);

    assertEquals("""
        O.effective as dateTime in Interval[@2021-01-01T, @2021-01-01T] or
        O.effective overlaps Interval[@2021-01-01T, @2021-01-01T]""", PrintContext.ZERO.print(expression));
  }
}
