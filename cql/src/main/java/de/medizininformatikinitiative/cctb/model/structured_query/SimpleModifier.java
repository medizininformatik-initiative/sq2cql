package de.medizininformatikinitiative.cctb.model.structured_query;

import de.medizininformatikinitiative.cctb.model.MappingContext;
import de.medizininformatikinitiative.cctb.model.cql.*;

/**
 * Simple modifiers only need to implement {@link #expression(MappingContext, IdentifierExpression)} to return a
 * boolean expression that will end up in the {@link WhereClause where clause} of the
 * query.
 */
public interface SimpleModifier extends Modifier {

    Container<DefaultExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias);

    @Override
    default Container<QueryExpression> updateQuery(MappingContext mappingContext,
                                                   Container<QueryExpression> queryContainer) {
        return queryContainer.flatMap(query -> expression(mappingContext, query.sourceAlias())
                .map(modifierExpr -> query.updateWhereClauseExpr(expr -> expr.and(modifierExpr))));
    }
}
