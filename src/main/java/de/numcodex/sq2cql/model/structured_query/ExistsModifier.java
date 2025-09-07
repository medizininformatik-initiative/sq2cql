package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.cql.Container;
import de.numcodex.sq2cql.model.cql.QueryExpression;
import de.numcodex.sq2cql.model.mapping.MappingContext;

public class ExistsModifier implements Modifier {

    @Override
    public Container<QueryExpression> updateQuery(MappingContext mappingContext, Container<QueryExpression> queryContainer) {
        return null;
    }
}
