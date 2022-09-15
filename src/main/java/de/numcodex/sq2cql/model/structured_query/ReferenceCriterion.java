package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AdditionExpressionTerm;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.cql.RetrieveExpression;
import de.numcodex.sq2cql.model.cql.ReturnClause;
import de.numcodex.sq2cql.model.cql.SourceClause;
import de.numcodex.sq2cql.model.cql.StringLiteralExpression;
import java.util.List;

/**
 * A {@code ReferenceCriterion} will select all patients that have at least one resource represented
 * by that concept through a reference. Currently, ReferenceCriterion is not defined
 * within the structured query (its virtual)
 */
public final class ReferenceCriterion extends AbstractCriterion {

  private final TermCode referencedTermCode;

  private ReferenceCriterion(Concept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, TermCode referencedTermCode) {
    super(concept, attributeFilters, timeRestriction);
    this.referencedTermCode = referencedTermCode;

  }

  public static ReferenceCriterion of(AbstractCriterion criterion, TermCode referencedTermCode) {
    return new ReferenceCriterion(criterion.concept, criterion.attributeFilters,
        criterion.timeRestriction(), referencedTermCode);
  }

  @Override
  Container<BooleanExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
      IdentifierExpression identifier) {
    var retrieveExprContainer = codeSelector(mappingContext, referencedTermCode).map(
        terminology -> RetrieveExpression.of("Medication", terminology));
    var alias = retrieveExprContainer.getExpression().get().alias();
    var returnClause = ReturnClause.of(
        AdditionExpressionTerm.of(StringLiteralExpression.of("Medication/"),
            InvocationExpression.of(alias, "id")));
    var queryExprContainer = retrieveExprContainer.map(
        retrieveExpr -> QueryExpression.of(SourceClause.of(retrieveExpr, retrieveExpr.alias()),
            returnClause));
    var valueExpr = InvocationExpression.of(identifier, mapping.valueFhirPath());
    var membershipExprContainer = Container.<BooleanExpression>of(
        MembershipExpression.in(valueExpr, referenceName(referencedTermCode)));
    return membershipExprContainer.addReferenceDefinition(referenceName(referencedTermCode).print(
            PrintContext.ZERO),
        queryExprContainer);
  }
}
