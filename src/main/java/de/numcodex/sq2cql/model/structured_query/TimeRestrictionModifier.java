package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.DateTimeExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.IntervalSelector;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.OrExpression;
import de.numcodex.sq2cql.model.cql.OverlapsIntervalOperatorPhrase;
import de.numcodex.sq2cql.model.cql.TypeExpression;

public final class TimeRestrictionModifier extends AbstractModifier{

  private final String beforeDate;
  private final String afterDate;

  private TimeRestrictionModifier(String path, String afterDate, String beforeDate) {
    super(path);
    this.afterDate = afterDate == null ? "1900-01-01T" : afterDate;
    this.beforeDate = beforeDate == null ? "2040-01-01T" : beforeDate;
  }

  public static TimeRestrictionModifier of(String path, String afterDate, String beforeDate) {
    return new TimeRestrictionModifier(path, afterDate, beforeDate);
  }

  public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression identifier) {
    var invocationExpr = InvocationExpression.of(identifier, path);
    var castExp = TypeExpression.of(invocationExpr, "dateTime");
    var intervalSelector = IntervalSelector.of(DateTimeExpression.of(afterDate), DateTimeExpression.of(beforeDate));
    var dateTimeInExpr = MembershipExpression.in(castExp, intervalSelector);
    var intervalOverlapExpr = OverlapsIntervalOperatorPhrase.of(invocationExpr, intervalSelector);
    return Container.of(OrExpression.of(dateTimeInExpr, intervalOverlapExpr));

  }
}
