package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ComparatorExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.TypeExpression;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A {@code NumericCriterion} will select all patients that have at least one resource represented
 * by that concept and numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class NumericCriterion extends AbstractCriterion {

  private final Comparator comparator;
  private final BigDecimal value;
  private final String unit;

  private NumericCriterion(Concept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, Comparator comparator,
      BigDecimal value, String unit) {
    super(concept, attributeFilters, timeRestriction);
    this.value = value;
    this.comparator = comparator;
    this.unit = unit;
  }

  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), null,
        requireNonNull(comparator),
        requireNonNull(value), null);
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept    the concept the criterion represents
   * @param comparator the comparator that should be used in the value comparison
   * @param value      the value that should be used in the value comparison
   * @param unit       the unit of the value
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      String unit) {
    return new NumericCriterion(concept, List.of(), null, requireNonNull(comparator),
        requireNonNull(value),
        requireNonNull(unit));
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), timeRestriction,
        requireNonNull(comparator),
        requireNonNull(value), null);
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @param unit             the unit of the value
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      String unit, TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), timeRestriction,
        requireNonNull(comparator),
        requireNonNull(value), requireNonNull(unit));
  }


  public Comparator getComparator() {
    return comparator;
  }

  public BigDecimal getValue() {
    return value;
  }

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }

  Container<BooleanExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
      IdentifierExpression identifier) {
    var castExpr = TypeExpression.of(InvocationExpression.of(identifier, mapping.valueFhirPath()),
        "Quantity");
    return Container.of(
        ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit)));
  }

  private Expression quantityExpression(BigDecimal value, String unit) {
    return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
  }
}
