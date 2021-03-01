package com.codex.sq2cql.data_model.cql.translator;

import com.codex.sq2cql.data_model.cql.*;
import com.codex.sq2cql.data_model.structured_query.Criterion;

import java.util.List;

public class CNFTranslator {
    public static BooleanExpression parseCNF(List<List<Criterion>> cnf) {
        var andExpression = new AndExpression();
        for(var conjunctions : cnf)
        {
            var orExpression = new OrExpression();
            for(var criterion : conjunctions)
            {
                orExpression.addExpression(new LiteralBoolExpression(criterion.getTermCode().getCode()));
            }
            andExpression.addExpression(orExpression);
        }
        return andExpression;
    }
}
