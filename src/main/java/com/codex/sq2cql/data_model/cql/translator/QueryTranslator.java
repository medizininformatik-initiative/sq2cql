package com.codex.sq2cql.data_model.cql.translator;

import com.codex.sq2cql.data_model.cql.AndExpression;
import com.codex.sq2cql.data_model.cql.BooleanExpression;
import com.codex.sq2cql.data_model.cql.NotExpression;
import com.codex.sq2cql.data_model.structured_query.Query;

import java.util.List;

public class QueryTranslator {
    public static BooleanExpression parseQuery(Query query)
    {
        var inclusionCriteria = CNFTranslator.parseCNF(query.getInclusionCriteria());
        var exclusionCriteria = DNFTranslator.parseDNF(query.getExclusionCriteria());
        var exclusionCriteriaNegation = new NotExpression(exclusionCriteria);
        return new AndExpression(List.of(inclusionCriteria, exclusionCriteriaNegation));
    }
}


