package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.QueryExpression;

/**
 * Simple modifiers only need to implement {@link #expression(MappingContext, IdentifierExpression)} to return a
 * boolean expression that will end up in the {@link de.numcodex.sq2cql.model.cql.WhereClause where clause} of the
 * query.
 */
public interface SimpleModifier extends Modifier {

    Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias);

    @Override
    default Container<QueryExpression> updateQuery(MappingContext mappingContext,
                                                   Container<QueryExpression> queryContainer) {
        return queryContainer.flatMap(query -> expression(mappingContext, query.sourceAlias())
                .map(modifierExpr -> query.updateWhereClauseExpr(expr -> expr.and(modifierExpr))));
    }
}
