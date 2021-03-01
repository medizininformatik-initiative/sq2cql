package com.codex.sq2cql.data_model.structured_query;

import com.codex.sq2cql.data_model.common.TermCode;

import java.util.List;

public class ValueSetCriterion extends Criterion{

    public ValueSetCriterion(TermCode concept, List<String> validValues) {
        super(concept);
        this.values = validValues;
    }

    private final List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void addValue(String value)
    {
        values.add(value);
    }
}
