package com.codex.sq2cql.data_model.structured_query;

import com.codex.sq2cql.data_model.common.Comparator;
import com.codex.sq2cql.data_model.common.TermCode;

public class NumericCriterion extends Criterion {
    public NumericCriterion(TermCode concept, double value, Comparator comparator) {
        super(concept);
        this.value = value;
        this.comparator = comparator;
    }

    private final double value;
    private String unit = "";
    private final Comparator comparator;

    public Comparator getComparator() {
        return comparator;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
