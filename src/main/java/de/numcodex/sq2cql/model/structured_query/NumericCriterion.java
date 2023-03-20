package de.numcodex.sq2cql.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.cql.AgeFunctionMapping;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ComparatorExpression;
import de.numcodex.sq2cql.model.cql.Expression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.QuantityExpression;
import de.numcodex.sq2cql.model.cql.TypeExpression;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A {@code NumericCriterion} will select all patients that have at least one resource represented
 * by that concept and numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class NumericCriterion extends AbstractCriterion<NumericCriterion> {

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
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value) {
    return new NumericCriterion(concept, List.of(), null,
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
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      TimeRestriction timeRestriction) {
    return new NumericCriterion(concept, List.of(), timeRestriction, requireNonNull(comparator),
        requireNonNull(value), null);
  }

  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param unit             the unit of the value
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(Concept concept, Comparator comparator, BigDecimal value,
      String unit, TimeRestriction timeRestriction) {
    return new NumericCriterion(concept, List.of(), timeRestriction, requireNonNull(comparator),
        requireNonNull(value), requireNonNull(unit));
  }

  @Override
  public NumericCriterion appendAttributeFilter(AttributeFilter attributeFilter) {
    var attributeFilters = new LinkedList<>(this.attributeFilters);
    attributeFilters.add(attributeFilter);
    return new NumericCriterion(concept, attributeFilters, timeRestriction, comparator, value, unit);
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

  @Override
  Container<BooleanExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
      IdentifierExpression identifier) {
    if (mapping.key().equals(AgeFunctionMapping.AGE)) {
      return ageExpr();
    }
    var castExpr = TypeExpression.of(InvocationExpression.of(identifier, mapping.valueFhirPath()),
        "Quantity");
    return Container.of(
        ComparatorExpression.of(castExpr, comparator, quantityExpression(value, unit)));
  }

  private Container<BooleanExpression> ageExpr() {
    var ageFunc = AgeFunctionMapping.getAgeFunction(unit);
    var quantity = QuantityExpression.of(value);
    var comparatorExpr = ComparatorExpression.of(ageFunc, comparator, quantity);
    return Container.of(comparatorExpr);
  }

  private Expression quantityExpression(BigDecimal value, String unit) {
    return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
  }
}
