package com.codex.sq2cql.data_model.cql.translator;

import com.codex.sq2cql.data_model.cql.*;
import com.codex.sq2cql.data_model.structured_query.Criterion;

import java.util.List;

public class DNFTranslator {
    public static BooleanExpression parseDNF(List<List<Criterion>> dnf) {
        var orExpression = new OrExpression();
        for(var conjunctions : dnf)
        {
            var andExpression = new AndExpression();
            for(var criterion : conjunctions)
            {
                andExpression.addExpression(new LiteralBoolExpression(criterion.getTermCode().getCode()));
            }
            orExpression.addExpression(andExpression);
        }
        return orExpression;
    }
}
