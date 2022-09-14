package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ExpressionDefinition;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.cql.RetrieveExpression;
import de.numcodex.sq2cql.model.cql.ReturnClause;
import de.numcodex.sq2cql.model.cql.SourceClause;
import java.util.List;
import java.util.Set;

/**
 * A {@code ReferenceCriterion} will select all patients that have at least one resource represented
 * by that concept through a reference.
 */
public final class ReferenceCriterion extends AbstractCriterion {

  private final TermCode referencedTermCode;

  private ReferenceCriterion(Concept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, TermCode referencedTermCode) {
    super(concept, attributeFilters, timeRestriction);
    this.referencedTermCode = referencedTermCode;

  }

  public static ReferenceCriterion from(AbstractCriterion criterion, TermCode referencedTermCode) {
    return new ReferenceCriterion(criterion.concept, criterion.attributeFilters,
        criterion.timeRestriction(), referencedTermCode);
  }

  @Override
  Container<BooleanExpression> valueExpr(MappingContext mappingContext, Mapping mapping,
      IdentifierExpression identifier) {
    var valueExpr = InvocationExpression.of(identifier, mapping.valueFhirPath());
    var codeSelector = codeSelector(mappingContext, referencedTermCode).getExpression();
    var retrieveExpr = RetrieveExpression.of("Medication", codeSelector.get());
    var query = QueryExpression.of(SourceClause.of(retrieveExpr, retrieveExpr.alias()),
        ReturnClause.of(IdentifierExpression.of("'Medication/' + M.id")));
    var defineExpr = new ExpressionDefinition("\"%s\"".formatted(referencedTermCode.display()), query);
    return Container.of(
        MembershipExpression.in(valueExpr, referenceName(mappingContext, referencedTermCode)),
        Set.of(), Set.of(defineExpr));
  }
}
